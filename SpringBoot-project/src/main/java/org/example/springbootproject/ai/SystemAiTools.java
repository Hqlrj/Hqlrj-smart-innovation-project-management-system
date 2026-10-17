package org.example.springbootproject.ai;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.example.springbootproject.service.AiToolExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SystemAiTools {

    @Autowired
    private AiToolExecutor toolExecutor;

    private final Gson gson = new Gson();

    @Tool("按学号查询项目。只有用户明确说出学号时才调用，不要因为项目名称去要学号。")
    public String searchProjectsByStudentId(@P("学生学号") String studentId) {
        JsonObject args = new JsonObject();
        args.addProperty("studentId", studentId);
        return toolExecutor.execute("search_projects_by_student_id", gson.toJson(args), AiCallerContext.get());
    }

    @Tool("按项目名称查询项目，返回 projectId。用户提到项目名、问某项目计划书或创新点时必须先调用。")
    public String searchProjectsByName(@P("项目名称") String projectName) {
        JsonObject args = new JsonObject();
        args.addProperty("projectName", projectName);
        return toolExecutor.execute("search_projects_by_name", gson.toJson(args), AiCallerContext.get());
    }

    @Tool("按项目ID查询项目简介、状态和成员名单。")
    public String getProjectBrief(@P("项目ID") Integer projectId) {
        JsonObject args = new JsonObject();
        args.addProperty("projectId", projectId);
        return toolExecutor.execute("get_project_brief", gson.toJson(args), AiCallerContext.get());
    }

    @Tool("检索计划书和公告正文。问创新点、技术方案、文档写了什么时必须调用。返回的是正文片段，请据此归纳，不要只看申报表字段。")
    public String searchDocuments(@P("检索问句") String query,
                                  @P("可选项目ID，不限定则传 null") Integer projectId) {
        JsonObject args = new JsonObject();
        args.addProperty("query", query);
        if (projectId != null) {
            args.addProperty("projectId", projectId);
        }
        return toolExecutor.execute("search_documents", gson.toJson(args), AiCallerContext.get());
    }
}
