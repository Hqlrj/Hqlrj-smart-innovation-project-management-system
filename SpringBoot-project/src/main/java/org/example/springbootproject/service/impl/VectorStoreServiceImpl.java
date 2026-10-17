package org.example.springbootproject.service.impl;

import com.google.gson.Gson;
import org.example.springbootproject.mapper.AiVectorChunkMapper;
import org.example.springbootproject.pojo.AiVectorChunk;
import org.example.springbootproject.pojo.Project;
import org.example.springbootproject.pojo.ProjectMember;
import org.example.springbootproject.pojo.User;
import org.example.springbootproject.service.EmbeddingService;
import org.example.springbootproject.service.ProjectService;
import org.example.springbootproject.service.VectorStoreService;
import org.example.springbootproject.util.CollegeScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class VectorStoreServiceImpl implements VectorStoreService {

    private static final Set<String> INNOVATION_HINTS = Set.of(
            "创新", "亮点", "特色", "突破", "关键技术"
    );

    @Autowired
    private AiVectorChunkMapper chunkMapper;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private ProjectService projectService;

    @Value("${ai.vector.top-k:4}")
    private int topK;

    private final Gson gson = new Gson();

    @Override
    public void upsertChunks(String sourceType, Integer sourceId, Integer projectId, String college,
                             String title, List<String> texts) {
        if (sourceType == null || sourceId == null) {
            return;
        }
        chunkMapper.deleteBySource(sourceType, sourceId);
        if (texts == null) {
            return;
        }
        for (String text : texts) {
            if (text == null || text.isBlank()) {
                continue;
            }
            AiVectorChunk chunk = new AiVectorChunk();
            chunk.setSourceType(sourceType);
            chunk.setSourceId(sourceId);
            chunk.setProjectId(projectId);
            chunk.setCollege(college);
            chunk.setTitle(title);
            chunk.setContent(text.trim());
            chunk.setEmbedding(embeddingService.serialize(embeddingService.embed(text)));
            chunkMapper.insert(chunk);
        }
    }

    @Override
    public void deleteBySource(String sourceType, Integer sourceId) {
        if (sourceType == null || sourceId == null) {
            return;
        }
        chunkMapper.deleteBySource(sourceType, sourceId);
    }

    @Override
    public String search(String query, Integer projectId, User caller) {
        if (caller == null) {
            return error("用户未登录，无法检索文档");
        }
        if (query == null || query.isBlank()) {
            return error("缺少检索内容");
        }
        String collegeFilter = CollegeScope.collegeFilter(caller, caller.getRole());
        List<AiVectorChunk> candidates;
        try {
            candidates = chunkMapper.listForSearch(collegeFilter, projectId);
        } catch (Exception e) {
            return error("向量库尚未初始化，请先执行 ai_vector_chunk.sql");
        }
        if (candidates == null || candidates.isEmpty()) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("total", 0);
            empty.put("message", "向量库中暂无该项目的计划书正文，请确认已上传 Word 并重启服务完成索引");
            empty.put("hits", List.of());
            empty.put("instruction", "没有检索到正文，不要根据申报表空字段下结论。");
            return gson.toJson(empty);
        }
        String expanded = expandQuery(query);
        float[] qv = embeddingService.embed(expanded);
        List<Map<String, Object>> ranked = new ArrayList<>();
        for (AiVectorChunk chunk : candidates) {
            if ("项目负责人".equals(caller.getRole())
                    && "plan".equals(chunk.getSourceType())
                    && !maySeeProject(caller, chunk.getProjectId())) {
                continue;
            }
            double vector = embeddingService.cosine(qv, embeddingService.deserialize(chunk.getEmbedding()));
            double keyword = keywordScore(expanded, chunk.getContent());
            double score = 0.4 * vector + 0.6 * keyword;
            if (projectId != null) {
                score += 0.05;
            }
            Map<String, Object> hit = new LinkedHashMap<>();
            hit.put("score", Math.round(score * 1000) / 1000.0);
            hit.put("sourceType", chunk.getSourceType());
            hit.put("sourceId", chunk.getSourceId());
            hit.put("projectId", chunk.getProjectId());
            hit.put("title", chunk.getTitle());
            hit.put("content", trim(chunk.getContent(), 380));
            ranked.add(hit);
        }
        ranked.sort(Comparator.comparingDouble((Map<String, Object> m) -> (Double) m.get("score")).reversed());
        if (ranked.size() > topK) {
            ranked = new ArrayList<>(ranked.subList(0, topK));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", ranked.size());
        result.put("query", query);
        result.put("hits", ranked);
        result.put("instruction", "只根据以上少量片段作答。回答最多3条，每条一句话。不要复述整篇文档，不要解释申报表是否为空。");
        return gson.toJson(result);
    }

    private String expandQuery(String query) {
        StringBuilder sb = new StringBuilder(query);
        if (query.contains("创新") || query.contains("亮点") || query.contains("特色")) {
            sb.append(' ').append(String.join(" ", INNOVATION_HINTS));
        }
        return sb.toString();
    }

    private double keywordScore(String query, String content) {
        if (content == null || content.isBlank()) {
            return 0;
        }
        String hay = content.toLowerCase(Locale.ROOT);
        String[] terms = query.toLowerCase(Locale.ROOT).split("[\\s,，。？?、]+");
        double hit = 0;
        double total = 0;
        for (String term : terms) {
            if (term.length() < 2) {
                continue;
            }
            total += 1;
            if (hay.contains(term)) {
                hit += term.length() >= 4 ? 1.5 : 1;
            }
        }
        if (total == 0) {
            return 0;
        }
        return Math.min(1.0, hit / total);
    }

    private boolean maySeeProject(User caller, Integer projectId) {
        if (projectId == null || caller == null) {
            return false;
        }
        Project project = projectService.getById(projectId);
        if (project == null) {
            return false;
        }
        if (caller.getId() != null && caller.getId().equals(project.getApplicantId())) {
            return true;
        }
        String sid = caller.getStudentId();
        if (sid == null || project.getMembers() == null) {
            return false;
        }
        for (ProjectMember member : project.getMembers()) {
            if (sid.equals(member.getStudentId())) {
                return true;
            }
        }
        return false;
    }

    private String trim(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }

    private String error(String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ok", false);
        result.put("message", message);
        return gson.toJson(result);
    }
}
