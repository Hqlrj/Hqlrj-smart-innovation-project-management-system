package org.example.springbootproject.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.example.springbootproject.constant.AiReviewConstants;
import org.example.springbootproject.mapper.AiReviewReportMapper;
import org.example.springbootproject.mapper.AiReviewSectionMapper;
import org.example.springbootproject.mapper.AiReviewTaskMapper;
import org.example.springbootproject.mapper.ProjectMapper;
import org.example.springbootproject.mapper.ProjectPlanMapper;
import org.example.springbootproject.pojo.AiReviewReport;
import org.example.springbootproject.pojo.AiReviewSection;
import org.example.springbootproject.pojo.AiReviewTask;
import org.example.springbootproject.pojo.DocumentParseResult;
import org.example.springbootproject.pojo.DocumentSection;
import org.example.springbootproject.pojo.Project;
import org.example.springbootproject.pojo.ProjectPlan;
import org.example.springbootproject.pojo.StartReviewRequest;
import org.example.springbootproject.pojo.StartReviewResponse;
import org.example.springbootproject.pojo.User;
import org.example.springbootproject.service.AiReviewService;
import org.example.springbootproject.service.DocumentParseService;
import org.example.springbootproject.service.SectionSplitService;
import org.example.springbootproject.service.UserService;
import org.example.springbootproject.util.AiReviewPromptUtil;
import org.example.springbootproject.util.DataCleaningUtil;
import org.example.springbootproject.util.DeepSeekApiUtil;
import org.example.springbootproject.util.SectionReviewPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * AI 评审：启动接口立即返回任务 ID，解析和模型调用在独立线程池中执行。
 */
@Service
public class AiReviewServiceImpl implements AiReviewService {

    private static final Logger log = LoggerFactory.getLogger(AiReviewServiceImpl.class);

    @Autowired
    private DeepSeekApiUtil deepSeekApiUtil;

    @Autowired
    private AiReviewTaskMapper aiReviewTaskMapper;

    @Autowired
    private AiReviewSectionMapper aiReviewSectionMapper;

    @Autowired
    private AiReviewReportMapper aiReviewReportMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectPlanMapper projectPlanMapper;

    @Autowired
    private SectionSplitService sectionSplitService;

    @Autowired
    private DocumentParseService documentParseService;

    @Autowired
    private DataCleaningUtil dataCleaningUtil;

    @Autowired
    private UserService userService;

    @Autowired
    @Qualifier("aiReviewExecutor")
    private Executor aiReviewExecutor;

    private final Gson gson = new Gson();

    @Override
    public StartReviewResponse startReview(Integer projectId, StartReviewRequest request, Integer userId) {
        try {
            Project project = projectMapper.getById(projectId);
            if (project == null) {
                return new StartReviewResponse(projectId, request.getMode(), null, null, "项目不存在");
            }

            AiReviewTask running = aiReviewTaskMapper.getLatestPendingTask(projectId);
            if (running != null) {
                return new StartReviewResponse(projectId, request.getMode(), null, running.getStatus(),
                        "该项目已有进行中的评审任务，请等待完成后再试");
            }

            String planPath = null;
            Integer planId = null;
            List<ProjectPlan> plans = projectPlanMapper.listByProjectId(projectId);
            if (plans != null && !plans.isEmpty()) {
                ProjectPlan firstPlan = plans.get(0);
                planId = firstPlan.getId();
                planPath = firstPlan.getFilePath();
                if ((project.getPlanFilePath() == null || project.getPlanFilePath().isEmpty()) && planPath != null) {
                    project.setPlanFilePath(firstPlan.getFilePath());
                    project.setPlanFileName(firstPlan.getFileName());
                    projectMapper.update(project);
                }
            } else if (project.getPlanFilePath() != null && !project.getPlanFilePath().isEmpty()) {
                planPath = project.getPlanFilePath();
            }

            if (planPath == null || planPath.isEmpty()) {
                return new StartReviewResponse(projectId, request.getMode(), null, null, "项目未上传计划书");
            }

            String fullPath = resolvePlanFullPath(planPath);
            if (fullPath == null || !Files.exists(Paths.get(fullPath))) {
                return new StartReviewResponse(projectId, request.getMode(), null, null, "计划书文件不存在，请重新上传");
            }

            String apiKey = null;
            if (AiReviewConstants.REVIEW_MODE_AI.equals(request.getMode())) {
                apiKey = request.getApiKey();
                if (apiKey == null || apiKey.trim().isEmpty()) {
                    User user = userService.getById(userId);
                    if (user == null || user.getApiKey() == null || user.getApiKey().trim().isEmpty()) {
                        return new StartReviewResponse(projectId, request.getMode(), null, null, "请先在个人设置中配置API Key");
                    }
                    apiKey = user.getApiKey();
                }
            }

            AiReviewTask task = new AiReviewTask();
            task.setProjectId(projectId);
            task.setUserId(userId);
            task.setStatus(AiReviewConstants.TASK_STATUS_PENDING);
            task.setCreateTime(LocalDateTime.now());
            task.setMode(request.getMode());
            task.setReviewMode(request.getMode());
            task.setPlanId(planId);
            aiReviewTaskMapper.insert(task);

            if (AiReviewConstants.REVIEW_MODE_AI.equals(request.getMode())) {
                final Integer taskId = task.getId();
                final String resolvedPath = fullPath;
                final String resolvedKey = apiKey;
                aiReviewExecutor.execute(() -> runAiReviewPipeline(taskId, resolvedPath, resolvedKey));
                return new StartReviewResponse(projectId, request.getMode(), task.getId(),
                        AiReviewConstants.TASK_STATUS_PENDING, "AI评审已启动");
            }

            task.setStatus(AiReviewConstants.TASK_STATUS_DONE);
            task.setEndTime(LocalDateTime.now());
            aiReviewTaskMapper.update(task);
            return new StartReviewResponse(projectId, request.getMode(), task.getId(),
                    task.getStatus(), "已进入人工审核");

        } catch (Exception e) {
            log.error("启动AI评审失败", e);
            return new StartReviewResponse(projectId, request.getMode(), null, null, "启动AI评审失败: " + e.getMessage());
        }
    }

    /**
     * 后台执行：解析 → 清洗 → 切分 → 逐章调用模型 → 汇总。
     */
    private void runAiReviewPipeline(Integer taskId, String fullPath, String apiKey) {
        try {
            if (!deepSeekApiUtil.isApiKeyConfigured(apiKey)) {
                aiReviewTaskMapper.recordFailure(taskId, "API Key未配置，请在个人设置中配置DeepSeek API Key");
                return;
            }

            AiReviewTask task = aiReviewTaskMapper.getById(taskId);
            if (task == null) {
                return;
            }

            aiReviewTaskMapper.updateStatus(taskId, AiReviewConstants.TASK_STATUS_PARSING);
            DocumentParseResult parseResult = documentParseService.parseDocument(fullPath);
            if (parseResult == null || parseResult.getParagraphs() == null || parseResult.getParagraphs().isEmpty()) {
                String errorMsg = "文档解析失败: " + (parseResult != null && parseResult.getErrorMsg() != null
                        ? parseResult.getErrorMsg() : "文档无有效段落");
                aiReviewTaskMapper.recordFailure(taskId, errorMsg);
                return;
            }

            aiReviewTaskMapper.updateStatus(taskId, AiReviewConstants.TASK_STATUS_CLEANING);
            DocumentParseResult cleaned = dataCleaningUtil.clean(parseResult);
            if (cleaned == null || cleaned.getParagraphs() == null || cleaned.getParagraphs().isEmpty()) {
                String errorMsg = cleaned != null && cleaned.getErrorMsg() != null
                        ? cleaned.getErrorMsg() : "数据清洗后没有可用段落";
                aiReviewTaskMapper.recordFailure(taskId, errorMsg);
                return;
            }

            aiReviewTaskMapper.updateStatus(taskId, AiReviewConstants.TASK_STATUS_SPLITTING);
            List<DocumentSection> sections = sectionSplitService.splitSectionsForTask(taskId, cleaned);
            if (sections == null || sections.isEmpty()) {
                aiReviewTaskMapper.recordFailure(taskId, "章节切分失败");
                return;
            }
            for (DocumentSection section : sections) {
                SectionReviewPolicy.applyKindAndWeight(section);
            }

            if (!executeSectionReview(taskId, sections, apiKey)) {
                return;
            }

            executeSummary(taskId, sections, apiKey);

        } catch (Exception e) {
            String errorMsg = "AI评审异常: " + truncate(e.getMessage(), 200);
            log.error("AI评审任务 {} 失败", taskId, e);
            aiReviewTaskMapper.recordFailure(taskId, errorMsg);
        }
    }

    private boolean executeSectionReview(Integer taskId, List<DocumentSection> sections, String apiKey) {
        try {
            aiReviewTaskMapper.updateStatus(taskId, AiReviewConstants.TASK_STATUS_REVIEWING);

            AiReviewTask task = aiReviewTaskMapper.getById(taskId);
            Project project = projectMapper.getById(task.getProjectId());

            int sectionDone = 0;
            int eligible = 0;

            for (int i = 0; i < sections.size(); i++) {
                DocumentSection section = sections.get(i);
                if (section.getCharCount() == null || section.getCharCount() < AiReviewConstants.MIN_SECTION_CHARS) {
                    continue;
                }
                eligible++;

                try {
                    AiReviewPromptUtil.PromptPair promptPair = AiReviewPromptUtil.buildSectionReviewPrompt(
                            project, section, i + 1, sections.size()
                    );

                    String response = deepSeekApiUtil.callDeepSeekApi(
                            promptPair.getSystemPrompt(),
                            promptPair.getUserPrompt(),
                            apiKey
                    );

                    JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
                    SectionReviewPolicy.Kind kind = section.getSectionKind() != null
                            ? SectionReviewPolicy.Kind.valueOf(section.getSectionKind())
                            : SectionReviewPolicy.classify(section.getSectionTitle());
                    List<SectionReviewPolicy.Dimension> expected = SectionReviewPolicy.dimensionsFor(kind);
                    Double sectionScore = calculateSectionScore(jsonResponse, expected);
                    if (sectionScore == null) {
                        updateSectionRecord(taskId, section, jsonResponse, "PARSE_FAIL");
                        continue;
                    }

                    section.setScore(sectionScore);
                    fillSectionOpinions(section, jsonResponse);
                    updateSectionRecord(taskId, section, jsonResponse, "SUCCESS");
                    sectionDone++;
                    aiReviewTaskMapper.updateProgress(taskId, sectionDone);

                } catch (Exception e) {
                    log.warn("章节评审失败: {}", section.getSectionTitle(), e);
                    updateSectionRecord(taskId, section, null, "FAIL:" + truncate(e.getMessage(), 40));
                }
            }

            if (eligible == 0) {
                aiReviewTaskMapper.recordFailure(taskId, "没有达到最小字数的可评审章节");
                return false;
            }
            if (sectionDone == 0) {
                aiReviewTaskMapper.recordFailure(taskId, "全部章节评审失败，请检查 API Key 或模型返回格式");
                return false;
            }

            return true;
        } catch (Exception e) {
            aiReviewTaskMapper.recordFailure(taskId, "章节评审失败: " + truncate(e.getMessage(), 200));
            return false;
        }
    }

    private boolean executeSummary(Integer taskId, List<DocumentSection> sections, String apiKey) {
        try {
            aiReviewTaskMapper.updateStatus(taskId, AiReviewConstants.TASK_STATUS_SUMMARIZING);

            AiReviewTask task = aiReviewTaskMapper.getById(taskId);
            Project project = projectMapper.getById(task.getProjectId());

            Double totalScore = calculateTotalScore(sections);
            if (totalScore == null) {
                aiReviewTaskMapper.recordFailure(taskId, "没有有效章节得分，无法汇总");
                return false;
            }
            String grade = calculateGrade(totalScore);
            List<String> missingSections = SectionReviewPolicy.findMissingSections(sections);

            AiReviewPromptUtil.PromptPair promptPair = AiReviewPromptUtil.buildSummaryPrompt(
                    project, sections, totalScore, grade, missingSections
            );

            String response = deepSeekApiUtil.callDeepSeekApi(
                    promptPair.getSystemPrompt(),
                    promptPair.getUserPrompt(),
                    apiKey
            );

            JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

            String summaryText = "";
            if (jsonResponse != null && jsonResponse.has("summary") && !jsonResponse.get("summary").isJsonNull()) {
                summaryText = jsonResponse.get("summary").getAsString();
            }

            task.setTotalScore(BigDecimal.valueOf(totalScore));
            task.setGrade(grade);
            task.setSummary(summaryText);
            task.setStatus(AiReviewConstants.TASK_STATUS_DONE);
            task.setEndTime(LocalDateTime.now());
            aiReviewTaskMapper.updateResult(task);
            saveReport(taskId, totalScore, grade, jsonResponse, missingSections);

            return true;
        } catch (Exception e) {
            aiReviewTaskMapper.recordFailure(taskId, "合并总结失败: " + truncate(e.getMessage(), 200));
            return false;
        }
    }

    /**
     * 只按本章应评维度、按维度名加权；缺维度则本章失败，不默认给分。
     */
    private Double calculateSectionScore(JsonObject jsonResponse, List<SectionReviewPolicy.Dimension> expected) {
        if (jsonResponse == null || expected == null || expected.isEmpty()
                || !jsonResponse.has("dimensions") || !jsonResponse.get("dimensions").isJsonArray()) {
            return null;
        }
        JsonArray dimensions = jsonResponse.getAsJsonArray("dimensions");
        java.util.Map<String, Double> scoreByName = new java.util.HashMap<>();
        for (int i = 0; i < dimensions.size(); i++) {
            if (!dimensions.get(i).isJsonObject()) {
                continue;
            }
            JsonObject dimension = dimensions.get(i).getAsJsonObject();
            if (!dimension.has("name") || !dimension.has("score")) {
                continue;
            }
            scoreByName.put(dimension.get("name").getAsString(), dimension.get("score").getAsDouble());
        }

        double weighted = 0;
        double weightSum = 0;
        for (SectionReviewPolicy.Dimension dim : expected) {
            Double score = scoreByName.get(dim.getName());
            if (score == null) {
                return null;
            }
            weighted += score * dim.getWeight();
            weightSum += dim.getWeight();
        }
        if (weightSum <= 0) {
            return null;
        }
        return weighted / weightSum;
    }

    /**
     * 仅统计有得分的章节，并按这些章节的权重重新归一化。
     */
    private Double calculateTotalScore(List<DocumentSection> sections) {
        if (sections == null || sections.isEmpty()) {
            return null;
        }

        double weighted = 0;
        double weightSum = 0;
        for (DocumentSection section : sections) {
            if (section.getScore() == null) {
                continue;
            }
            double weight = section.getWeight() != null && section.getWeight() > 0
                    ? section.getWeight()
                    : 0.08;
            if (weight <= 0) {
                continue;
            }
            weighted += section.getScore() * 20 * weight;
            weightSum += weight;
        }
        if (weightSum <= 0) {
            return null;
        }
        return weighted / weightSum;
    }

    private String calculateGrade(double totalScore) {
        if (totalScore >= AiReviewConstants.GRADE_A_THRESHOLD) {
            return AiReviewConstants.GRADE_A;
        } else if (totalScore >= AiReviewConstants.GRADE_B_THRESHOLD) {
            return AiReviewConstants.GRADE_B;
        } else if (totalScore >= AiReviewConstants.GRADE_C_THRESHOLD) {
            return AiReviewConstants.GRADE_C;
        }
        return AiReviewConstants.GRADE_D;
    }

    private void updateSectionRecord(Integer taskId, DocumentSection section, JsonObject jsonResponse, String status) {
        AiReviewSection aiSection = new AiReviewSection();
        aiSection.setTaskId(taskId);
        aiSection.setSortOrder(section.getSortOrder());
        aiSection.setSectionTitle(section.getSectionTitle());
        aiSection.setSectionLevel(section.getSectionLevel());
        aiSection.setCharCount(section.getCharCount());
        aiSection.setScore(section.getScore());
        aiSection.setStatus(status.length() > 15 ? status.substring(0, 15) : status);
        aiSection.setCreateTime(LocalDateTime.now());

        if (jsonResponse != null) {
            aiSection.setDimensions(jsonResponse.has("dimensions") ? jsonResponse.get("dimensions").toString() : null);
            aiSection.setStrengths(jsonResponse.has("strengths") ? convertJsonArrayToText(jsonResponse.getAsJsonArray("strengths")) : null);
            aiSection.setWeaknesses(jsonResponse.has("weaknesses") ? convertJsonArrayToText(jsonResponse.getAsJsonArray("weaknesses")) : null);
            aiSection.setRisks(jsonResponse.has("risks") ? convertJsonArrayToText(jsonResponse.getAsJsonArray("risks")) : null);
            aiSection.setSuggestions(jsonResponse.has("suggestions") ? convertJsonArrayToText(jsonResponse.getAsJsonArray("suggestions")) : null);
            aiSection.setRawResponse(jsonResponse.toString());
        }

        try {
            aiReviewSectionMapper.insert(aiSection);
        } catch (Exception e) {
            log.error("保存章节记录失败", e);
        }
    }

    private void fillSectionOpinions(DocumentSection section, JsonObject jsonResponse) {
        if (jsonResponse == null) {
            return;
        }
        if (jsonResponse.has("strengths")) {
            section.setStrengths(convertJsonArrayToText(jsonResponse.getAsJsonArray("strengths")));
        }
        if (jsonResponse.has("weaknesses")) {
            section.setWeaknesses(convertJsonArrayToText(jsonResponse.getAsJsonArray("weaknesses")));
        }
        if (jsonResponse.has("risks")) {
            section.setRisks(convertJsonArrayToText(jsonResponse.getAsJsonArray("risks")));
        }
        if (jsonResponse.has("suggestions")) {
            section.setSuggestions(convertJsonArrayToText(jsonResponse.getAsJsonArray("suggestions")));
        }
    }

    private void saveReport(Integer taskId, double totalScore, String grade,
                            JsonObject jsonResponse, List<String> missingSections) {
        try {
            AiReviewReport report = new AiReviewReport();
            report.setTaskId(taskId);
            report.setTotalScore(BigDecimal.valueOf(totalScore));
            report.setGrade(grade);
            report.setCreateTime(LocalDateTime.now());
            if (jsonResponse != null) {
                report.setSummary(jsonFieldAsString(jsonResponse, "summary"));
                report.setHighlights(jsonElementAsText(jsonResponse, "highlights"));
                report.setMainProblems(jsonElementAsText(jsonResponse, "main_problems"));
                report.setRisks(jsonElementAsText(jsonResponse, "risks"));
                report.setSuggestions(jsonElementAsText(jsonResponse, "suggestions"));
                report.setAiRecommendation(jsonFieldAsString(jsonResponse, "ai_recommendation"));
                report.setConsistencyNote(jsonFieldAsString(jsonResponse, "consistency_note"));
                report.setRawJson(jsonResponse.toString());
            }
            report.setMissingSections(gson.toJson(missingSections == null ? List.of() : missingSections));
            aiReviewReportMapper.deleteByTaskId(taskId);
            aiReviewReportMapper.insert(report);
        } catch (Exception e) {
            log.error("保存AI评审报告失败, taskId={}", taskId, e);
        }
    }

    private String jsonFieldAsString(JsonObject json, String field) {
        if (json == null || !json.has(field) || json.get(field).isJsonNull()) {
            return null;
        }
        if (json.get(field).isJsonPrimitive()) {
            return json.get(field).getAsString();
        }
        return json.get(field).toString();
    }

    private String jsonElementAsText(JsonObject json, String field) {
        if (json == null || !json.has(field) || json.get(field).isJsonNull()) {
            return null;
        }
        return json.get(field).toString();
    }

    private String convertJsonArrayToText(JsonArray jsonArray) {
        if (jsonArray == null || jsonArray.size() == 0) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < jsonArray.size(); i++) {
            if (i > 0) {
                result.append("\n");
            }
            com.google.gson.JsonElement element = jsonArray.get(i);
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                result.append(element.getAsString());
            } else if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                if (obj.has("point")) {
                    result.append(obj.get("point").getAsString());
                } else {
                    result.append(element.toString());
                }
            } else {
                result.append(element.toString());
            }
        }
        return result.toString();
    }

    private String resolvePlanFullPath(String planFilePath) {
        if (planFilePath == null || planFilePath.isBlank()) {
            return null;
        }
        Path given = Paths.get(planFilePath);
        if (given.isAbsolute() && Files.exists(given)) {
            return given.toAbsolutePath().normalize().toString();
        }
        String normalized = planFilePath.replace("\\", "/");
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        Path resolved = Paths.get(System.getProperty("user.dir")).resolve(normalized).normalize();
        return resolved.toString();
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() > max ? text.substring(0, max) : text;
    }

    @Override
    public AiReviewTask getAiReviewTask(Integer taskId) {
        return aiReviewTaskMapper.getById(taskId);
    }

    @Override
    public List<AiReviewSection> getAiReviewSections(Integer taskId) {
        return aiReviewSectionMapper.listByTaskId(taskId);
    }
}
