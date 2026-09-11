package org.example.springbootproject.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.springbootproject.mapper.ProjectPlanMapper;
import org.example.springbootproject.pojo.PageBean;
import org.example.springbootproject.pojo.Project;
import org.example.springbootproject.pojo.ProjectPlan;
import org.example.springbootproject.pojo.Result;
import org.example.springbootproject.service.NotificationService;
import org.example.springbootproject.service.ProjectService;
import org.example.springbootproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 项目申报控制器
 * 提供项目申报相关的RESTful API接口
 */
@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectPlanMapper projectPlanMapper;

    // 文件上传路径（相对于项目根目录）
    private static final String UPLOAD_DIR = "uploads/project-plans/";

    /**
     * 分页查询项目列表（项目查询页面使用）
     */
    @GetMapping
    public Result<PageBean<Project>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            String projectName, String projectType, String status, String projectSpace, Integer applicantId,
            HttpServletRequest request) {
        // 移除权限检查：所有用户都可以查看所有项目
        PageBean<Project> pageBean = projectService.list(page, pageSize, projectName, projectType, status, projectSpace, applicantId);
        return Result.success(pageBean);
    }

    /**
     * 根据ID查询项目详情
     */
    @GetMapping("/{id}")
    public Result<Project> getById(@PathVariable Integer id) {
        Project project = projectService.getById(id);
        if (project == null) {
            return Result.error("项目不存在");
        }
        return Result.success(project);
    }

    /**
     * 新增项目
     */
    @PostMapping
    public Result<Project> add(@RequestBody Project project, HttpServletRequest request) {
        // 从JWT中获取当前用户信息
        Integer userId = (Integer) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        
        // 设置申请人信息
        project.setApplicantId(userId);
        if (project.getApplicantName() == null || project.getApplicantName().isEmpty()) {
            // 从用户服务获取用户姓名
            var user = userService.getById(userId);
            if (user != null) {
                project.setApplicantName(user.getName());
            } else {
                project.setApplicantName(username);
            }
        }
        
        projectService.add(project);
        // 返回创建的项目（包含ID）
        return Result.success(project);
    }

    /**
     * 更新项目
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id, @RequestBody Project project) {
        project.setId(id);
        projectService.update(project);
        return Result.success();
    }

    /**
     * 删除项目
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        projectService.delete(id);
        return Result.success();
    }

    /**
     * 提交项目申报
     */
    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Integer id) {
        projectService.submit(id);
        return Result.success();
    }

    /**
     * 上传计划书文件（支持多文件上传）
     * 所有用户都可以上传
     */
    @PostMapping("/upload-plan")
    public Result<List<ProjectPlan>> uploadPlan(@RequestParam("files") MultipartFile[] files, 
                                      @RequestParam(value = "projectId", required = false) Integer projectId,
                                      HttpServletRequest request) {
        // 移除权限检查：所有用户都可以上传计划书
        if (files == null || files.length == 0) {
            return Result.error("文件不能为空");
        }

        List<ProjectPlan> uploadedPlans = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try {
            // 使用绝对路径创建上传目录
            String projectRoot = System.getProperty("user.dir");
            File uploadDir = new File(projectRoot, UPLOAD_DIR);
            if (!uploadDir.exists()) {
                if (!uploadDir.mkdirs()) {
                    return Result.error("无法创建上传目录: " + uploadDir.getAbsolutePath());
                }
            }
            if (!uploadDir.isDirectory()) {
                return Result.error("上传路径不是一个目录: " + uploadDir.getAbsolutePath());
            }
            if (!uploadDir.canWrite()) {
                return Result.error("上传目录不可写: " + uploadDir.getAbsolutePath());
            }

            // 处理每个文件
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    errors.add("文件为空，跳过");
                    continue;
                }

                // 检查文件类型（允许Word文档和PDF）
                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null) {
                    errors.add("文件名不能为空");
                    continue;
                }
                String lowerFilename = originalFilename.toLowerCase();
                if (!lowerFilename.endsWith(".doc") && !lowerFilename.endsWith(".docx") && !lowerFilename.endsWith(".pdf")) {
                    errors.add(originalFilename + ": 只支持上传Word文档（.doc或.docx格式）或PDF文件（.pdf格式）");
                    continue;
                }

                try {
                    // 生成唯一文件名
                    int lastDotIndex = originalFilename.lastIndexOf(".");
                    if (lastDotIndex == -1) {
                        errors.add(originalFilename + ": 文件名缺少扩展名");
                        continue;
                    }
                    String fileExtension = originalFilename.substring(lastDotIndex);
                    String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
                    File targetFile = new File(uploadDir, uniqueFileName);

                    // 保存文件
                    try {
                        file.transferTo(targetFile);
                        // 验证文件是否成功保存
                        if (!targetFile.exists() || targetFile.length() == 0) {
                            errors.add(originalFilename + ": 文件保存失败，文件可能为空或保存异常");
                            if (targetFile.exists()) {
                                targetFile.delete();
                            }
                            continue;
                        }
                    } catch (IOException e) {
                        errors.add(originalFilename + ": 文件保存失败 - " + e.getMessage());
                        if (targetFile.exists()) {
                            targetFile.delete();
                        }
                        continue;
                    }

                    // 统一使用正斜杠作为路径分隔符（用于Web访问）
                    String filePath = UPLOAD_DIR + uniqueFileName;
                    String normalizedPath = filePath.replace("\\", "/");

                    // 创建计划书记录
                    ProjectPlan plan = new ProjectPlan();
                    plan.setProjectId(projectId);
                    plan.setFilePath(normalizedPath);
                    plan.setFileName(originalFilename);
                    plan.setFileSize(file.getSize());
                    plan.setCreateTime(LocalDateTime.now());

                    // 如果提供了projectId，保存到数据库
                    if (projectId != null) {
                        try {
                            projectPlanMapper.insert(plan);
                        } catch (Exception e) {
                            // 数据库保存失败，删除已上传的文件
                            if (targetFile.exists()) {
                                targetFile.delete();
                            }
                            errors.add(originalFilename + ": 数据库保存失败 - " + e.getMessage());
                            continue;
                        }
                    }

                    uploadedPlans.add(plan);
                } catch (Exception e) {
                    // 捕获所有其他异常
                    errors.add(originalFilename + ": 处理失败 - " + e.getMessage());
                    e.printStackTrace();
                }
            }

            if (uploadedPlans.isEmpty()) {
                String errorMsg = errors.isEmpty() 
                    ? "所有文件上传失败，请检查文件是否为空或格式是否正确" 
                    : "所有文件上传失败: " + String.join(", ", errors);
                return Result.error(errorMsg);
            }

            if (!errors.isEmpty()) {
                // 部分成功，返回成功列表（错误信息可以通过日志记录）
                // 注意：Result.success 只接受一个参数，错误信息可以通过其他方式返回
                return Result.success(uploadedPlans);
            }

            return Result.success(uploadedPlans);

        } catch (Exception e) {
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 删除计划书文件
     * 所有用户都可以删除
     */
    @DeleteMapping("/plan/{id}")
    public Result<Void> deletePlan(@PathVariable Integer id, HttpServletRequest request) {
        // 移除权限检查：所有用户都可以删除计划书
        try {
            // 查询计划书信息
            ProjectPlan plan = projectPlanMapper.getById(id);
            if (plan == null) {
                return Result.error("计划书不存在");
            }

            // 删除文件
            String projectRoot = System.getProperty("user.dir");
            File file = new File(projectRoot, plan.getFilePath());
            if (file.exists()) {
                if (!file.delete()) {
                    return Result.error("文件删除失败");
                }
            }

            // 删除数据库记录
            projectPlanMapper.delete(id);

            return Result.success();
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 下载计划书模板
     */
    @GetMapping("/download-template")
    public Result<String> downloadTemplate() {
        // 返回模板文件路径（实际项目中可以提供一个模板文件）
        String templatePath = "templates/project-plan-template.docx";
        return Result.success(templatePath);
    }

    /**
     * 审批项目（通过）
     */
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Integer id, HttpServletRequest request) {
        // 从JWT中获取当前用户ID
        Integer userId = (Integer) request.getAttribute("userId");
        // 查询用户信息获取真实姓名
        var user = userService.getById(userId);
        String auditorName = user != null ? user.getName() : (String) request.getAttribute("username");
        projectService.approve(id, auditorName);
        // 发送通知给项目申请人
        Project project = projectService.getById(id);
        if (project != null && project.getApplicantId() != null) {
            notificationService.createNotification(
                project.getApplicantId(),
                "项目审批通知",
                "您申报的项目「" + project.getProjectName() + "」已通过审批",
                "project_approve", project.getId());
        }
        return Result.success();
    }

    /**
     * 驳回项目
     */
    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Integer id,
                               @RequestBody(required = false) java.util.Map<String, String> requestBody,
                               HttpServletRequest request) {
        String reason = requestBody != null ? requestBody.get("reason") : null;
        // 从JWT中获取当前用户ID
        Integer userId = (Integer) request.getAttribute("userId");
        // 查询用户信息获取真实姓名
        var user = userService.getById(userId);
        String auditorName = user != null ? user.getName() : (String) request.getAttribute("username");
        projectService.reject(id, auditorName, reason);
        // 发送通知给项目申请人
        Project project = projectService.getById(id);
        if (project != null && project.getApplicantId() != null) {
            notificationService.createNotification(
                project.getApplicantId(),
                "项目审批通知",
                "您申报的项目「" + project.getProjectName() + "」已被驳回",
                "project_reject", project.getId());
        }
        return Result.success();
    }

    /**
     * 获取当前用户创建的项目列表（用于下拉选择）
     */
    @GetMapping("/my-projects")
    public Result<List<Project>> getMyProjects(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        PageBean<Project> pageBean = projectService.list(1, 1000, null, null, null, null, userId);
        return Result.success(pageBean.getList());
    }
}
