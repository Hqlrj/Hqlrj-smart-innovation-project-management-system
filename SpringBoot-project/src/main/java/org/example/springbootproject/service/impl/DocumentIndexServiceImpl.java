package org.example.springbootproject.service.impl;

import org.example.springbootproject.mapper.ProjectPlanMapper;
import org.example.springbootproject.pojo.DocumentParagraph;
import org.example.springbootproject.pojo.DocumentParseResult;
import org.example.springbootproject.pojo.Notice;
import org.example.springbootproject.pojo.Project;
import org.example.springbootproject.pojo.ProjectPlan;
import org.example.springbootproject.pojo.User;
import org.example.springbootproject.service.DocumentIndexService;
import org.example.springbootproject.service.DocumentParseService;
import org.example.springbootproject.service.ProjectService;
import org.example.springbootproject.service.UserService;
import org.example.springbootproject.service.VectorStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentIndexServiceImpl implements DocumentIndexService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIndexServiceImpl.class);
    private static final int CHUNK_SIZE = 650;
    private static final int CHUNK_OVERLAP = 120;

    @Autowired
    private VectorStoreService vectorStoreService;

    @Autowired
    private DocumentParseService documentParseService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectPlanMapper projectPlanMapper;

    @Override
    public void indexNotice(Notice notice) {
        if (notice == null || notice.getId() == null) {
            return;
        }
        try {
            String college = notice.getPublisherCollege();
            if ((college == null || college.isBlank()) && notice.getPublisherId() != null) {
                User publisher = userService.getById(notice.getPublisherId());
                if (publisher != null) {
                    college = publisher.getCollege();
                }
            }
            if ("系统管理员".equals(notice.getPublisherRole())) {
                college = null;
            }
            String body = (notice.getTitle() == null ? "" : notice.getTitle() + "\n")
                    + (notice.getContent() == null ? "" : notice.getContent());
            vectorStoreService.upsertChunks("notice", notice.getId(), null, college,
                    notice.getTitle(), split(body));
        } catch (Exception e) {
            log.warn("索引公告失败: {}", e.getMessage());
        }
    }

    @Override
    public void indexPlan(Integer projectId, Integer planId, String filePath, String fileName) {
        if (planId == null) {
            log.warn("索引计划书跳过：planId 为空");
            return;
        }
        try {
            Project project = projectId == null ? null : projectService.getById(projectId);
            String college = project == null ? null : project.getApplicantCollege();
            List<String> chunks = new ArrayList<>();
            String projectName = project == null ? "" : nullToEmpty(project.getProjectName());
            String prefix = "【计划书正文】项目《" + projectName + "》，文件《" + nullToEmpty(fileName) + "》。";
            StringBuilder form = new StringBuilder();
            appendIfPresent(form, "申报简介", project == null ? null : project.getProjectIntro());
            appendIfPresent(form, "申报表创新点", project == null ? null : project.getInnovationPoints());
            appendIfPresent(form, "申报背景", project == null ? null : project.getProjectBackground());
            appendIfPresent(form, "关键技术", project == null ? null : project.getKeyTechnology());
            if (!form.isEmpty()) {
                chunks.add(prefix + "以下为申报表已填写内容（可能不完整，主体仍以计划书正文为准）：\n" + form);
            }

            File word = resolveFile(filePath);
            if (word != null) {
                DocumentParseResult parsed = documentParseService.parseDocument(word.getAbsolutePath());
                if (parsed != null && parsed.getErrorMsg() != null) {
                    log.warn("解析计划书失败 {}: {}", word.getAbsolutePath(), parsed.getErrorMsg());
                }
                if (parsed != null && parsed.getParagraphs() != null) {
                    StringBuilder text = new StringBuilder();
                    for (DocumentParagraph paragraph : parsed.getParagraphs()) {
                        if (paragraph.getText() != null && !paragraph.getText().isBlank()) {
                            text.append(paragraph.getText().trim()).append('\n');
                        }
                    }
                    chunks.addAll(split(prefix + text));
                }
            } else {
                log.warn("索引计划书找不到文件: {}", filePath);
            }

            vectorStoreService.upsertChunks("plan", planId, projectId, college,
                    fileName == null ? projectName : fileName, chunks);
            log.info("已索引计划书 planId={} projectId={} chunks={}", planId, projectId, chunks.size());
        } catch (Exception e) {
            log.warn("索引计划书失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public void reindexExistingPlans() {
        int indexed = 0;
        try {
            List<Project> projects = projectService.list(1, 10000, null, null, null, null, null, null).getList();
            if (projects != null) {
                for (Project project : projects) {
                    if (project.getPlanFilePath() == null || project.getPlanFilePath().isBlank()) {
                        continue;
                    }
                    List<ProjectPlan> existing = projectPlanMapper.listByProjectId(project.getId());
                    boolean hasFile = existing != null && existing.stream()
                            .anyMatch(p -> project.getPlanFilePath().equals(p.getFilePath()));
                    if (!hasFile) {
                        ProjectPlan plan = new ProjectPlan();
                        plan.setProjectId(project.getId());
                        plan.setFilePath(project.getPlanFilePath());
                        plan.setFileName(project.getPlanFileName());
                        plan.setCreateTime(java.time.LocalDateTime.now());
                        projectPlanMapper.insert(plan);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("从项目表补计划书记录失败: {}", e.getMessage());
        }
        List<ProjectPlan> plans;
        try {
            plans = projectPlanMapper.listAll();
        } catch (Exception e) {
            log.warn("回填计划书索引失败，无法读取 project_plan: {}", e.getMessage());
            return;
        }
        if (plans == null || plans.isEmpty()) {
            log.info("没有已上传的计划书需要回填索引");
            return;
        }
        for (ProjectPlan plan : plans) {
            indexPlan(plan.getProjectId(), plan.getId(), plan.getFilePath(), plan.getFileName());
            indexed++;
        }
        log.info("已回填 {} 份计划书到向量库", indexed);
    }

    @Override
    public void deleteNotice(Integer noticeId) {
        vectorStoreService.deleteBySource("notice", noticeId);
    }

    @Override
    public void deletePlan(Integer planId) {
        vectorStoreService.deleteBySource("plan", planId);
    }

    private List<String> split(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null) {
            return chunks;
        }
        String cleaned = text.trim();
        if (cleaned.isEmpty()) {
            return chunks;
        }
        int i = 0;
        while (i < cleaned.length()) {
            int end = Math.min(i + CHUNK_SIZE, cleaned.length());
            if (end < cleaned.length()) {
                int breakAt = Math.max(cleaned.lastIndexOf('\n', end), cleaned.lastIndexOf('。', end));
                if (breakAt > i + CHUNK_SIZE / 3) {
                    end = breakAt + 1;
                }
            }
            chunks.add(cleaned.substring(i, end).trim());
            if (end >= cleaned.length()) {
                break;
            }
            i = Math.max(i + 1, end - CHUNK_OVERLAP);
        }
        return chunks;
    }

    private void appendIfPresent(StringBuilder sb, String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        sb.append(label).append('：').append(value.trim()).append('\n');
    }

    private File resolveFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return null;
        }
        String normalized = filePath.replace("\\", "/");
        List<File> candidates = new ArrayList<>();
        File given = new File(filePath);
        candidates.add(given);
        String userDir = System.getProperty("user.dir");
        candidates.add(new File(userDir, filePath));
        candidates.add(new File(userDir, normalized));
        File parent = new File(userDir).getParentFile();
        if (parent != null) {
            candidates.add(new File(parent, filePath));
            candidates.add(new File(new File(parent, "SpringBoot-project"), filePath));
        }
        candidates.add(new File(new File(userDir, "SpringBoot-project"), filePath));
        for (File file : candidates) {
            if (file.isFile() && file.length() > 0) {
                return file;
            }
        }
        return null;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
