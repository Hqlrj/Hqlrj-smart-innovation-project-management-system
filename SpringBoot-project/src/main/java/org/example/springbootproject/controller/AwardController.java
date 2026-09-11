package org.example.springbootproject.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.springbootproject.pojo.Award;
import org.example.springbootproject.pojo.PageBean;
import org.example.springbootproject.pojo.Project;
import org.example.springbootproject.pojo.Result;
import org.example.springbootproject.service.AwardService;
import org.example.springbootproject.service.NotificationService;
import org.example.springbootproject.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * 获奖记录控制器
 * 提供获奖记录相关的RESTful API接口
 */
@RestController
@RequestMapping("/awards")
public class AwardController {

    @Autowired
    private AwardService awardService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private NotificationService notificationService;

    // 文件上传路径（获奖证明图片）
    private static final String UPLOAD_DIR = "uploads/award-certificates/";

    /**
     * 分页查询获奖记录列表（支持条件查询）
     */
    @GetMapping
    public Result<PageBean<Award>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            String projectName, String competitionName, String competitionLevel, String awardLevel,
            @RequestParam(required = false) Integer applicantId,
            HttpServletRequest request) {
        // 移除权限检查：所有用户都可以查看所有获奖记录
        PageBean<Award> pageBean = awardService.list(page, pageSize, projectName, competitionName, 
                                                      competitionLevel, awardLevel, applicantId);
        return Result.success(pageBean);
    }

    /**
     * 根据项目ID查询获奖记录列表
     */
    @GetMapping("/project/{projectId}")
    public Result<List<Award>> listByProjectId(@PathVariable Integer projectId) {
        List<Award> awards = awardService.listByProjectId(projectId);
        return Result.success(awards);
    }

    /**
     * 根据ID查询获奖记录
     */
    @GetMapping("/{id}")
    public Result<Award> getById(@PathVariable Integer id) {
        Award award = awardService.getById(id);
        return Result.success(award);
    }

    /**
     * 新增获奖记录
     */
    @PostMapping
    public Result<Award> add(@RequestBody Award award) {
        awardService.add(award);
        return Result.success(award);
    }

    /**
     * 更新获奖记录
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id, @RequestBody Award award) {
        award.setId(id);
        awardService.update(award);
        return Result.success();
    }

    /**
     * 删除获奖记录
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        awardService.delete(id);
        return Result.success();
    }

    /**
     * 审批获奖记录（通过）
     */
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Integer id) {
        awardService.approve(id);
        // 发送通知给项目申请人
        Award award = awardService.getById(id);
        if (award != null) {
            Project project = projectService.getById(award.getProjectId());
            Integer targetUserId = project != null ? project.getApplicantId() : null;
            if (targetUserId != null) {
                notificationService.createNotification(
                    targetUserId,
                    "获奖记录审批通知",
                    "您项目「" + award.getProjectName() + "」的获奖记录「" + award.getCompetitionName() + "」已通过审批",
                    "award_approve", award.getId());
            }
        }
        return Result.success();
    }

    /**
     * 审批获奖记录（未通过）
     */
    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Integer id, @RequestBody(required = false) java.util.Map<String, String> requestBody) {
        // 从JSON对象中提取reason字段
        String reason = requestBody != null ? requestBody.get("reason") : null;
        awardService.reject(id, reason);
        // 发送通知给项目申请人
        Award award = awardService.getById(id);
        if (award != null) {
            Project project = projectService.getById(award.getProjectId());
            Integer targetUserId = project != null ? project.getApplicantId() : null;
            if (targetUserId != null) {
                notificationService.createNotification(
                    targetUserId,
                    "获奖记录审批通知",

                    "您项目「" + award.getProjectName() + "」的获奖记录「" + award.getCompetitionName() + "」未通过审批",
                    "award_reject", award.getId());
            }
        }
        return Result.success();
    }

    /**
     * 上传获奖证明图片
     */
    @PostMapping("/upload-certificate")
    public Result<String> uploadCertificate(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        // 检查文件类型（只允许图片）
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || 
            (!originalFilename.endsWith(".jpg") && !originalFilename.endsWith(".jpeg") && 
             !originalFilename.endsWith(".png") && !originalFilename.endsWith(".gif"))) {
            return Result.error("只支持上传图片文件（jpg、jpeg、png、gif格式）");
        }

        try {
            // 获取项目根目录
            String projectRoot = System.getProperty("user.dir");
            // 构建完整的上传目录路径
            Path uploadPath = Paths.get(projectRoot, UPLOAD_DIR);
            
            // 创建目录（如果不存在）
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(uniqueFileName);

            // 保存文件
            file.transferTo(filePath.toFile());

            // 返回相对路径（使用正斜杠，因为这是Web路径）
            String relativePath = UPLOAD_DIR + uniqueFileName;
            return Result.success(relativePath);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }
}
