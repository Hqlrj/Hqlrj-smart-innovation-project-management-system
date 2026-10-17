package org.example.springbootproject.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.springbootproject.pojo.Project;
import org.example.springbootproject.pojo.ProjectMember;
import org.example.springbootproject.pojo.User;
import org.example.springbootproject.service.AiToolExecutor;
import org.example.springbootproject.service.ProjectService;
import org.example.springbootproject.service.VectorStoreService;
import org.example.springbootproject.util.CollegeScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiToolExecutorImpl implements AiToolExecutor {

    private static final int MAX_PROJECTS = 20;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private VectorStoreService vectorStoreService;

    private final Gson gson = new Gson();

    @Override
    public JsonArray toolDefinitions() {
        JsonArray tools = new JsonArray();
        tools.add(functionTool(
                "search_projects_by_student_id",
                "仅当用户明确给出学号时使用。不要用本工具查询项目名称。",
                studentIdSchema()
        ));
        tools.add(functionTool(
                "search_projects_by_name",
                "按项目名称模糊查询项目。用户提到项目名、问某项目计划书时必须先调用本工具拿到 projectId。",
                projectNameSchema()
        ));
        tools.add(functionTool(
                "get_project_brief",
                "按项目ID查询项目简介、状态和成员名单（不含手机号等敏感信息）。用户追问某个项目详情时调用。",
                projectIdSchema()
        ));
        tools.add(functionTool(
                "search_documents",
                "在公告和项目计划书向量库中检索文档内容。用户问计划书写了什么、公告内容时调用。",
                documentSchema()
        ));
        return tools;
    }

    @Override
    public String execute(String toolName, String argumentsJson, User caller) {
        if (caller == null) {
            return errorJson("用户未登录，无法查询系统数据");
        }
        try {
            JsonObject args = parseArgs(argumentsJson);
            if ("search_projects_by_student_id".equals(toolName)) {
                return searchProjectsByStudentId(args, caller);
            }
            if ("search_projects_by_name".equals(toolName)) {
                return searchProjectsByName(args, caller);
            }
            if ("get_project_brief".equals(toolName)) {
                return getProjectBrief(args, caller);
            }
            if ("search_documents".equals(toolName)) {
                return vectorStoreService.search(text(args, "query"), intValue(args, "projectId"), caller);
            }
            return errorJson("未知工具: " + toolName);
        } catch (Exception e) {
            return errorJson("工具执行失败: " + e.getMessage());
        }
    }

    private String searchProjectsByStudentId(JsonObject args, User caller) {
        String studentId = text(args, "studentId");
        if (studentId.isEmpty()) {
            return errorJson("缺少学号参数 studentId");
        }
        if ("项目负责人".equals(caller.getRole())
                && (caller.getStudentId() == null || !studentId.equals(caller.getStudentId().trim()))) {
            return errorJson("项目负责人只能查询自己学号关联的项目");
        }
        String collegeFilter = CollegeScope.collegeFilter(caller, caller.getRole());
        List<Project> projects = projectService.listByStudentId(studentId, collegeFilter, MAX_PROJECTS);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("studentId", studentId);
        result.put("total", projects.size());
        if (projects.isEmpty()) {
            result.put("message", "当前权限范围内没有该学号关联的项目");
            result.put("projects", List.of());
            return gson.toJson(result);
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (Project p : projects) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("projectId", p.getId());
            item.put("projectName", p.getProjectName());
            item.put("projectType", p.getProjectType());
            item.put("status", p.getStatus());
            item.put("applicantName", p.getApplicantName());
            item.put("college", p.getApplicantCollege());
            item.put("relation", p.getRelation());
            items.add(item);
        }
        result.put("projects", items);
        return gson.toJson(result);
    }

    private String searchProjectsByName(JsonObject args, User caller) {
        String projectName = text(args, "projectName");
        if (projectName.isEmpty()) {
            return errorJson("缺少项目名称参数 projectName");
        }
        String collegeFilter = CollegeScope.collegeFilter(caller, caller.getRole());
        var page = projectService.list(1, 10, projectName, null, null, null, null, collegeFilter);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("keyword", projectName);
        if (page == null || page.getList() == null || page.getList().isEmpty()) {
            result.put("total", 0);
            result.put("message", "没有找到名称匹配的项目");
            result.put("projects", List.of());
            return gson.toJson(result);
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (Project p : page.getList()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("projectId", p.getId());
            item.put("projectName", p.getProjectName());
            item.put("projectType", p.getProjectType());
            item.put("status", p.getStatus());
            item.put("applicantName", p.getApplicantName());
            item.put("college", p.getApplicantCollege());
            item.put("innovationPoints", trimText(p.getInnovationPoints(), 200));
            items.add(item);
        }
        result.put("total", items.size());
        result.put("projects", items);
        return gson.toJson(result);
    }

    private String getProjectBrief(JsonObject args, User caller) {
        Integer projectId = intValue(args, "projectId");
        if (projectId == null) {
            return errorJson("缺少项目ID参数 projectId");
        }
        Project project = projectService.getById(projectId);
        if (project == null) {
            return errorJson("项目不存在");
        }
        String collegeFilter = CollegeScope.collegeFilter(caller, caller.getRole());
        if (!CollegeScope.canViewCollege(caller.getRole(), collegeFilter, project.getApplicantCollege())) {
            return errorJson("无权查看该项目");
        }
        if ("项目负责人".equals(caller.getRole()) && !participates(project, caller)) {
            return errorJson("只能查看自己参与的项目");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectId", project.getId());
        result.put("projectName", project.getProjectName());
        result.put("projectType", project.getProjectType());
        result.put("status", project.getStatus());
        result.put("applicantName", project.getApplicantName());
        result.put("college", project.getApplicantCollege());
        result.put("intro", trimText(project.getProjectIntro(), 400));
        List<Map<String, String>> members = new ArrayList<>();
        if (project.getMembers() != null) {
            for (ProjectMember member : project.getMembers()) {
                Map<String, String> row = new LinkedHashMap<>();
                row.put("name", member.getName());
                row.put("studentId", member.getStudentId());
                row.put("college", member.getCollege());
                row.put("className", member.getClassName());
                members.add(row);
            }
        }
        result.put("members", members);
        return gson.toJson(result);
    }

    private boolean participates(Project project, User caller) {
        if (caller.getId() != null && caller.getId().equals(project.getApplicantId())) {
            return true;
        }
        String sid = caller.getStudentId();
        if (sid == null || sid.isBlank() || project.getMembers() == null) {
            return false;
        }
        for (ProjectMember member : project.getMembers()) {
            if (sid.equals(member.getStudentId())) {
                return true;
            }
        }
        return false;
    }

    private JsonObject parseArgs(String argumentsJson) {
        if (argumentsJson == null || argumentsJson.isBlank()) {
            return new JsonObject();
        }
        return JsonParser.parseString(argumentsJson).getAsJsonObject();
    }

    private String text(JsonObject args, String key) {
        if (!args.has(key) || args.get(key).isJsonNull()) {
            return "";
        }
        return args.get(key).getAsString().trim();
    }

    private Integer intValue(JsonObject args, String key) {
        if (!args.has(key) || args.get(key).isJsonNull()) {
            return null;
        }
        try {
            return args.get(key).getAsInt();
        } catch (Exception e) {
            try {
                return Integer.parseInt(args.get(key).getAsString().trim());
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    private String trimText(String value, int max) {
        if (value == null) {
            return "";
        }
        String text = value.trim();
        if (text.length() <= max) {
            return text;
        }
        return text.substring(0, max) + "...";
    }

    private String errorJson(String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ok", false);
        result.put("message", message);
        return gson.toJson(result);
    }

    private JsonObject functionTool(String name, String description, JsonObject parameters) {
        JsonObject fn = new JsonObject();
        fn.addProperty("name", name);
        fn.addProperty("description", description);
        fn.add("parameters", parameters);
        JsonObject tool = new JsonObject();
        tool.addProperty("type", "function");
        tool.add("function", fn);
        return tool;
    }

    private JsonObject studentIdSchema() {
        JsonObject props = new JsonObject();
        JsonObject studentId = new JsonObject();
        studentId.addProperty("type", "string");
        studentId.addProperty("description", "学生学号，例如 666");
        props.add("studentId", studentId);
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        schema.add("properties", props);
        JsonArray required = new JsonArray();
        required.add("studentId");
        schema.add("required", required);
        return schema;
    }

    private JsonObject projectNameSchema() {
        JsonObject props = new JsonObject();
        JsonObject projectName = new JsonObject();
        projectName.addProperty("type", "string");
        projectName.addProperty("description", "项目名称或其中一部分，例如 皇睿杰");
        props.add("projectName", projectName);
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        schema.add("properties", props);
        JsonArray required = new JsonArray();
        required.add("projectName");
        schema.add("required", required);
        return schema;
    }

    private JsonObject documentSchema() {
        JsonObject props = new JsonObject();
        JsonObject query = new JsonObject();
        query.addProperty("type", "string");
        query.addProperty("description", "检索问句，例如：创新点写了什么");
        props.add("query", query);
        JsonObject projectId = new JsonObject();
        projectId.addProperty("type", "integer");
        projectId.addProperty("description", "可选，限定某个项目的计划书");
        props.add("projectId", projectId);
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        schema.add("properties", props);
        JsonArray required = new JsonArray();
        required.add("query");
        schema.add("required", required);
        return schema;
    }

    private JsonObject projectIdSchema() {
        JsonObject props = new JsonObject();
        JsonObject projectId = new JsonObject();
        projectId.addProperty("type", "integer");
        projectId.addProperty("description", "项目ID");
        props.add("projectId", projectId);
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        schema.add("properties", props);
        JsonArray required = new JsonArray();
        required.add("projectId");
        schema.add("required", required);
        return schema;
    }
}
