package org.example.springbootproject.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.springbootproject.constant.AiReviewConstants;
import org.example.springbootproject.mapper.AiReviewSectionMapper;
import org.example.springbootproject.mapper.AiReviewTaskMapper;
import org.example.springbootproject.mapper.ProjectMapper;
import org.example.springbootproject.mapper.ProjectPlanMapper;
import org.example.springbootproject.pojo.*;
import org.example.springbootproject.service.AiReviewService;
import org.example.springbootproject.service.DocumentParseService;
import org.example.springbootproject.service.SectionSplitService;
import org.example.springbootproject.service.UserService;
import org.example.springbootproject.util.AiReviewPromptUtil;
import org.example.springbootproject.util.DeepSeekApiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * AI 评审服务实现类
 * 实现AI评审相关的业务逻辑处理
 */
@Service
public class AiReviewServiceImpl implements AiReviewService {

    @Autowired
    private DeepSeekApiUtil deepSeekApiUtil;

    @Autowired
    private AiReviewTaskMapper aiReviewTaskMapper;

    @Autowired
    private AiReviewSectionMapper aiReviewSectionMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectPlanMapper projectPlanMapper;

    @Autowired
    private SectionSplitService sectionSplitService;

    @Autowired
    private DocumentParseService documentParseService;

    @Autowired
    private UserService userService;

    private final Gson gson = new Gson();

    /**
     * 发起AI评审
     * @param projectId 项目ID
     * @param request 评审请求
     * @param userId 用户ID
     * @return 评审响应
     */
    @Override
    public StartReviewResponse startReview(Integer projectId, StartReviewRequest request, Integer userId) {
        try {
            // 获取项目信息
            Project project = projectMapper.getById(projectId);
            if (project == null) {
                return new StartReviewResponse(projectId, request.getMode(), null, null, "项目不存在");
            }

            // 检查是否有计划书（检查project表或project_plan表）
            boolean hasPlan = false;
            Integer planId = null;
            
            // 总是查询 project_plan 表获取 planId
            List<ProjectPlan> plans = projectPlanMapper.listByProjectId(projectId);
            System.out.println("项目 " + projectId + " 的计划书数量: " + (plans != null ? plans.size() : 0));
            
            if (plans != null && !plans.isEmpty()) {
                hasPlan = true;
                planId = plans.get(0).getId();
                System.out.println("找到计划书，planId: " + planId);
                
                // 如果project表没有计划书路径，更新它
                if (project.getPlanFilePath() == null || project.getPlanFilePath().isEmpty()) {
                    ProjectPlan firstPlan = plans.get(0);
                    project.setPlanFilePath(firstPlan.getFilePath());
                    project.setPlanFileName(firstPlan.getFileName());
                    projectMapper.update(project);
                    System.out.println("已更新project表计划书路径: " + firstPlan.getFilePath());
                }
            } else {
                // project_plan表也没有计划书
                System.out.println("项目 " + projectId + " 在project_plan表中也没有计划书");
                if (project.getPlanFilePath() != null && !project.getPlanFilePath().isEmpty()) {
                    hasPlan = true;
                    System.out.println("但project表中有计划书路径: " + project.getPlanFilePath());
                }
            }
            
            if (!hasPlan) {
                System.out.println("项目 " + projectId + " 未上传计划书");
                return new StartReviewResponse(projectId, request.getMode(), null, null, "项目未上传计划书");
            }

            // 如果是AI模式，需要API Key
            if ("AI".equals(request.getMode())) {
                String apiKey = request.getApiKey();
                
                // 如果前端没有传递API Key，则从用户配置中获取
                if (apiKey == null || apiKey.trim().isEmpty()) {
                    User user = userService.getById(userId);
                    if (user == null || user.getApiKey() == null || user.getApiKey().trim().isEmpty()) {
                        return new StartReviewResponse(projectId, request.getMode(), null, null, "请先在个人设置中配置API Key");
                    }
                    apiKey = user.getApiKey();
                }
                
                // 临时设置API Key（本次请求有效）
                deepSeekApiUtil.setTemporaryApiKey(apiKey);
            }

            // 创建AI评审任务
            AiReviewTask task = new AiReviewTask();
            task.setProjectId(projectId);
            task.setUserId(userId);
            task.setStatus(AiReviewConstants.TASK_STATUS_PENDING);
            task.setCreateTime(LocalDateTime.now());
            task.setMode(request.getMode());
            task.setReviewMode(request.getMode()); // 同时设置reviewMode字段
            task.setPlanId(planId); // 使用前面获取的 planId
            
            System.out.println("创建AI审核任务，planId: " + planId);
            
            aiReviewTaskMapper.insert(task);

            // 解析文档
            String projectRoot = System.getProperty("user.dir");
            String fullPath;
            
            // 处理路径分隔符，兼容Windows和Unix
            String planPath = project.getPlanFilePath().replace("\\", "/");
            if (planPath.startsWith("/")) {
                fullPath = projectRoot + planPath;
            } else {
                fullPath = projectRoot + "/" + planPath;
            }
            
            // Windows系统处理
            fullPath = fullPath.replace("/", "\\");
            
            System.out.println("准备解析文档，完整路径: " + fullPath);
            
            DocumentParseResult parseResult = documentParseService.parseDocument(fullPath);
            
            System.out.println("文档解析结果: " + (parseResult != null ? "成功" : "失败"));
            if (parseResult != null) {
                System.out.println("段落数量: " + (parseResult.getParagraphs() != null ? parseResult.getParagraphs().size() : 0));
            }
            
            if (parseResult == null || parseResult.getParagraphs() == null || parseResult.getParagraphs().isEmpty()) {
                String errorMsg = "文档解析失败: " + (parseResult != null && parseResult.getErrorMsg() != null ? parseResult.getErrorMsg() : "未知原因");
                System.out.println(errorMsg);
                aiReviewTaskMapper.recordFailure(task.getId(), errorMsg);
                return new StartReviewResponse(projectId, request.getMode(), task.getId(), AiReviewConstants.TASK_STATUS_FAILED, errorMsg);
            }

            // 异步执行AI评审
            if ("AI".equals(request.getMode())) {
                executeAiReview(task.getId(), parseResult);
            } else {
                // 人工模式，直接标记为完成
                task.setStatus(AiReviewConstants.TASK_STATUS_DONE);
                task.setEndTime(LocalDateTime.now());
                aiReviewTaskMapper.update(task);
            }

            return new StartReviewResponse(projectId, request.getMode(), task.getId(), task.getStatus(), "AI评审已启动");

        } catch (Exception e) {
            System.out.println("启动AI评审异常: " + e.getMessage());
            e.printStackTrace();
            return new StartReviewResponse(projectId, request.getMode(), null, null, "启动AI评审失败: " + e.getMessage());
        }
    }

    /**
     * 执行完整的AI评审流程
     * @param taskId 任务ID
     * @param parseResult 文档解析结果
     * @return 是否成功
     */
    @Override
    @Async
    public CompletableFuture<Boolean> executeAiReview(Integer taskId, DocumentParseResult parseResult) {
        try {
            // 检查API Key是否配置
            if (!deepSeekApiUtil.isApiKeyConfigured()) {
                aiReviewTaskMapper.recordFailure(taskId, "API Key未配置，请在系统设置中配置DeepSeek API Key");
                return CompletableFuture.completedFuture(false);
            }

            // 获取项目信息
            AiReviewTask task = aiReviewTaskMapper.getById(taskId);
            if (task == null) {
                return CompletableFuture.completedFuture(false);
            }

            Project project = projectMapper.getById(task.getProjectId());
            if (project == null) {
                return CompletableFuture.completedFuture(false);
            }

            // 更新状态为PARSING（文档解析）
            System.out.println("任务ID " + taskId + " 更新状态为 PARSING");
            aiReviewTaskMapper.updateStatus(taskId, AiReviewConstants.TASK_STATUS_PARSING);
            
            // 添加延迟，让前端能够看到状态变化
            Thread.sleep(2000);

            // 执行章节切分
            List<DocumentSection> sections = sectionSplitService.splitSectionsForTask(taskId, parseResult);
            
            if (sections == null || sections.isEmpty()) {
                aiReviewTaskMapper.recordFailure(taskId, "章节切分失败");
                return CompletableFuture.completedFuture(false);
            }

            // 更新状态为SPLITTING（章节切分完成）
            System.out.println("任务ID " + taskId + " 更新状态为 SPLITTING");
            aiReviewTaskMapper.updateStatus(taskId, AiReviewConstants.TASK_STATUS_SPLITTING);
            
            // 添加延迟，让前端能够看到状态变化
            Thread.sleep(2000);

            // 执行章节评审
            boolean sectionReviewSuccess = executeSectionReview(taskId, sections);
            
            if (!sectionReviewSuccess) {
                return CompletableFuture.completedFuture(false);
            }

            // 执行合并总结
            boolean summarySuccess = executeSummary(taskId, sections);
            
            return CompletableFuture.completedFuture(summarySuccess);

        } catch (Exception e) {
            String errorMsg = "AI评审异常: " + (e.getMessage() != null && e.getMessage().length() > 200 ? e.getMessage().substring(0, 200) : e.getMessage());
            aiReviewTaskMapper.recordFailure(taskId, errorMsg);
            e.printStackTrace();
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * 执行章节评审
     * @param taskId 任务ID
     * @param sections 章节列表
     * @return 是否成功
     */
    @Override
    public boolean executeSectionReview(Integer taskId, List<DocumentSection> sections) {
        try {
            // 更新任务状态为REVIEWING
            aiReviewTaskMapper.updateStatus(taskId, AiReviewConstants.TASK_STATUS_REVIEWING);

            AiReviewTask task = aiReviewTaskMapper.getById(taskId);
            Project project = projectMapper.getById(task.getProjectId());

            int sectionDone = 0;
            int totalScore = 0; // 用于累加，后续改为加权计算

            for (int i = 0; i < sections.size(); i++) {
                DocumentSection section = sections.get(i);
                
                // 跳过内容过少的章节
                if (section.getCharCount() < AiReviewConstants.MIN_SECTION_CHARS) {
                    continue;
                }

                try {
                    // 生成提示词
                    AiReviewPromptUtil.PromptPair promptPair = AiReviewPromptUtil.buildSectionReviewPrompt(
                        project, section, i + 1, sections.size()
                    );

                    // 调用DeepSeek API
                    String response = deepSeekApiUtil.callDeepSeekApi(
                        promptPair.getSystemPrompt(),
                        promptPair.getUserPrompt()
                    );

                    // 解析响应
                    JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
                    
                    // 提取评分
                    double sectionScore = calculateSectionScore(jsonResponse);
                    section.setScore(sectionScore);

                    // 更新章节记录
                    updateSectionRecord(taskId, section, jsonResponse, "SUCCESS");

                    sectionDone++;

                } catch (Exception e) {
                    // 单章失败，记录错误但不影响其他章节
                    String errorMsg = "章节评审失败: " + (e.getMessage() != null && e.getMessage().length() > 100 ? e.getMessage().substring(0, 100) : e.getMessage());
                    updateSectionRecord(taskId, section, null, errorMsg);
                }
            }

            // 更新任务进度
            aiReviewTaskMapper.updateProgress(taskId, sectionDone);

            return true;

        } catch (Exception e) {
            aiReviewTaskMapper.recordFailure(taskId, "章节评审失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 执行合并总结
     * @param taskId 任务ID
     * @param sections 章节列表
     * @return 是否成功
     */
    @Override
    public boolean executeSummary(Integer taskId, List<DocumentSection> sections) {
        try {
            // 更新任务状态为SUMMARIZING
            aiReviewTaskMapper.updateStatus(taskId, AiReviewConstants.TASK_STATUS_SUMMARIZING);

            AiReviewTask task = aiReviewTaskMapper.getById(taskId);
            Project project = projectMapper.getById(task.getProjectId());

            // 计算综合得分（加权平均）
            double totalScore = calculateTotalScore(sections);
            String grade = calculateGrade(totalScore);

            // 生成总结提示词
            AiReviewPromptUtil.PromptPair promptPair = AiReviewPromptUtil.buildSummaryPrompt(
                project, sections, totalScore, grade
            );

            // 调用DeepSeek API
            String response = deepSeekApiUtil.callDeepSeekApi(
                promptPair.getSystemPrompt(),
                promptPair.getUserPrompt()
            );

            // 解析响应
            JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
            
            // 将summary从JSON转换为纯文本
            String summaryText = "";
            if (jsonResponse.has("summary")) {
                summaryText = jsonResponse.get("summary").getAsString();
            }
            
            // 更新任务结果
            task.setTotalScore(new BigDecimal(totalScore));
            task.setGrade(grade);
            task.setSummary(summaryText); // 设置纯文本的summary
            task.setStatus(AiReviewConstants.TASK_STATUS_DONE);
            task.setEndTime(LocalDateTime.now());
            aiReviewTaskMapper.updateResult(task);

            // 保存报告
            saveReport(taskId, jsonResponse);

            return true;

        } catch (Exception e) {
            aiReviewTaskMapper.recordFailure(taskId, "合并总结失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 计算章节得分
     * @param jsonResponse AI响应JSON
     * @return 章节得分（0-5）
     */
    private double calculateSectionScore(JsonObject jsonResponse) {
        try {
            if (jsonResponse.has("dimensions") && jsonResponse.getAsJsonArray("dimensions").size() > 0) {
                double totalScore = 0;
                double[] weights = {0.25, 0.25, 0.25, 0.25}; // 完整性、创新性、可行性、论证充分性
                
                for (int i = 0; i < jsonResponse.getAsJsonArray("dimensions").size(); i++) {
                    JsonObject dimension = jsonResponse.getAsJsonArray("dimensions").get(i).getAsJsonObject();
                    if (dimension.has("score")) {
                        totalScore += dimension.get("score").getAsDouble() * weights[i];
                    }
                }
                
                return totalScore;
            }
        } catch (Exception e) {
            // 解析失败，返回默认分数
        }
        return 3.0; // 默认3分
    }

    /**
     * 计算综合得分（加权平均）
     * @param sections 章节列表
     * @return 综合得分（0-100）
     */
    private double calculateTotalScore(List<DocumentSection> sections) {
        double totalScore = 0;
        
        if (sections == null || sections.isEmpty()) {
            return totalScore;
        }

        for (DocumentSection section : sections) {
            if (section.getScore() != null && section.getWeight() != null) {
                // 章节得分(0-5) × 20 = 0-100分制
                totalScore += section.getScore() * 20 * section.getWeight();
            }
        }
        
        return totalScore;
    }

    /**
     * 计算等级
     * @param totalScore 综合得分
     * @return 等级
     */
    private String calculateGrade(double totalScore) {
        if (totalScore >= AiReviewConstants.GRADE_A_THRESHOLD) {
            return AiReviewConstants.GRADE_A;
        } else if (totalScore >= AiReviewConstants.GRADE_B_THRESHOLD) {
            return AiReviewConstants.GRADE_B;
        } else if (totalScore >= AiReviewConstants.GRADE_C_THRESHOLD) {
            return AiReviewConstants.GRADE_C;
        } else {
            return AiReviewConstants.GRADE_D;
        }
    }

    /**
     * 更新章节记录
     * @param taskId 任务ID
     * @param section 章节信息
     * @param jsonResponse AI响应JSON
     * @param status 状态
     */
    private void updateSectionRecord(Integer taskId, DocumentSection section, JsonObject jsonResponse, String status) {
        AiReviewSection aiSection = new AiReviewSection();
        aiSection.setTaskId(taskId);
        aiSection.setSortOrder(section.getSortOrder());
        aiSection.setSectionTitle(section.getSectionTitle());
        aiSection.setSectionLevel(section.getSectionLevel());
        aiSection.setCharCount(section.getCharCount());
        aiSection.setScore(section.getScore());
        aiSection.setStatus(status.length() > 15 ? status.substring(0, 15) : status); // 限制status长度
        aiSection.setCreateTime(LocalDateTime.now());
        
        if (jsonResponse != null) {
            aiSection.setDimensions(jsonResponse.has("dimensions") ? jsonResponse.get("dimensions").toString() : null);
            
            // 将JSON数组转换为纯文本字符串
            aiSection.setStrengths(jsonResponse.has("strengths") ? convertJsonArrayToText(jsonResponse.getAsJsonArray("strengths")) : null);
            aiSection.setWeaknesses(jsonResponse.has("weaknesses") ? convertJsonArrayToText(jsonResponse.getAsJsonArray("weaknesses")) : null);
            aiSection.setRisks(jsonResponse.has("risks") ? convertJsonArrayToText(jsonResponse.getAsJsonArray("risks")) : null);
            aiSection.setSuggestions(jsonResponse.has("suggestions") ? convertJsonArrayToText(jsonResponse.getAsJsonArray("suggestions")) : null);
            aiSection.setRawResponse(jsonResponse.toString());
        }

        try {
            aiReviewSectionMapper.insert(aiSection);
        } catch (Exception e) {
            System.out.println("保存章节记录失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 将JSON数组转换为纯文本字符串
     * @param jsonArray JSON数组
     * @return 纯文本字符串
     */
    private String convertJsonArrayToText(com.google.gson.JsonArray jsonArray) {
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
                // 如果是字符串，直接添加
                result.append(element.getAsString());
            } else if (element.isJsonObject()) {
                // 如果是对象，尝试提取point字段
                JsonObject obj = element.getAsJsonObject();
                if (obj.has("point")) {
                    result.append(obj.get("point").getAsString());
                } else {
                    // 如果没有point字段，直接添加toString
                    result.append(element.toString());
                }
            } else {
                // 其他情况，直接添加toString
                result.append(element.toString());
            }
        }
        
        return result.toString();
    }

    /**
     * 保存报告
     * @param taskId 任务ID
     * @param jsonResponse AI响应JSON
     */
    private void saveReport(Integer taskId, JsonObject jsonResponse) {
        // TODO: 实现报告保存到 ai_review_report 表
        // 暂时跳过，后续可以添加
    }

    /**
     * 获取AI评审任务详情
     * @param taskId 任务ID
     * @return 任务详情
     */
    @Override
    public AiReviewTask getAiReviewTask(Integer taskId) {
        return aiReviewTaskMapper.getById(taskId);
    }
    
    /**
     * 获取AI评审章节详情
     * @param taskId 任务ID
     * @return 章节列表
     */
    @Override
    public List<org.example.springbootproject.pojo.AiReviewSection> getAiReviewSections(Integer taskId) {
        return aiReviewSectionMapper.listByTaskId(taskId);
    }
}
