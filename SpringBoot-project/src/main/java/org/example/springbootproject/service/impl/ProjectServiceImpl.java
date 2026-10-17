package org.example.springbootproject.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.example.springbootproject.mapper.AwardMapper;
import org.example.springbootproject.mapper.ProjectMapper;
import org.example.springbootproject.mapper.ProjectMemberMapper;
import org.example.springbootproject.mapper.ProjectAdvisorMapper;
import org.example.springbootproject.mapper.ProjectPlanMapper;
import org.example.springbootproject.pojo.Award;
import org.example.springbootproject.pojo.PageBean;
import org.example.springbootproject.pojo.Project;
import org.example.springbootproject.pojo.ProjectMember;
import org.example.springbootproject.pojo.ProjectAdvisor;
import org.example.springbootproject.pojo.ProjectPlan;
import org.example.springbootproject.service.DocumentIndexService;
import org.example.springbootproject.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目服务实现类
 * 实现项目相关的业务逻辑处理
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectMemberMapper projectMemberMapper;

    @Autowired
    private ProjectAdvisorMapper projectAdvisorMapper;

    @Autowired
    private AwardMapper awardMapper;

    @Autowired
    private ProjectPlanMapper projectPlanMapper;

    @Autowired
    @Lazy
    private DocumentIndexService documentIndexService;

    /**
     * 分页查询项目列表
     */
    @Override
    public PageBean<Project> list(Integer page, Integer pageSize, String projectName, String projectType, String status, String projectSpace, Integer applicantId, String applicantCollege) {
        // 设置分页参数
        PageHelper.startPage(page, pageSize);

        // 执行查询
        List<Project> projectList = projectMapper.list(projectName, projectType, status, projectSpace, applicantId, applicantCollege);

        // 获取分页信息
        Page<Project> p = (Page<Project>) projectList;

        // 封装分页结果
        return PageBean.of(p.getTotal(), p.getResult());
    }

    @Override
    public List<Project> listByStudentId(String studentId, String applicantCollege, Integer limit) {
        int size = (limit == null || limit <= 0) ? 20 : Math.min(limit, 20);
        return projectMapper.listByStudentId(studentId, applicantCollege, size);
    }

    /**
     * 根据ID查询项目（包含成员列表、计划书和获奖记录）
     */
    @Override
    public Project getById(Integer id) {
        Project project = projectMapper.getById(id);
        if (project != null) {
            try {
                List<ProjectMember> members = projectMemberMapper.listByProjectId(id);
                project.setMembers(members != null ? members : List.of());
            } catch (Exception e) {
                project.setMembers(List.of());
            }

            try {
                List<ProjectAdvisor> advisors = projectAdvisorMapper.listByProjectId(id);
                project.setAdvisors(advisors != null ? advisors : List.of());
            } catch (Exception e) {
                project.setAdvisors(List.of());
            }

            try {
                List<Award> allAwards = awardMapper.listByProjectId(id);
                if (allAwards == null) {
                    allAwards = List.of();
                }
                List<Award> approvedAwards = allAwards.stream()
                        .filter(award -> "approved".equals(award.getStatus()))
                        .collect(Collectors.toList());
                project.setAwards(approvedAwards);
            } catch (Exception e) {
                project.setAwards(List.of());
            }

            List<ProjectPlan> plans = List.of();
            try {
                List<ProjectPlan> loaded = projectPlanMapper.listByProjectId(id);
                if (loaded != null) {
                    plans = loaded;
                }
            } catch (Exception e) {
                plans = List.of();
            }
            
            // 兼容旧数据：如果 plans 为空，但项目表中已经有 planFilePath / planFileName，
            // 则根据这两个字段构造一个虚拟的计划书记录，保证前端“计划书”标签页能看到已上传的文件
            if ((plans == null || plans.isEmpty())
                    && project.getPlanFilePath() != null && !project.getPlanFilePath().isEmpty()
                    && project.getPlanFileName() != null && !project.getPlanFileName().isEmpty()) {
                ProjectPlan virtualPlan = new ProjectPlan();
                virtualPlan.setProjectId(project.getId());
                virtualPlan.setFilePath(project.getPlanFilePath());
                virtualPlan.setFileName(project.getPlanFileName());
                plans = List.of(virtualPlan);
            }
            
            project.setPlans(plans);
            
            // 如果计划书列表不为空，将第一个计划书的信息同步到 planFilePath 和 planFileName
            // 这样可以确保详情页面能正确显示计划书
            if (plans != null && !plans.isEmpty()) {
                ProjectPlan firstPlan = plans.get(0);
                if (firstPlan.getFilePath() != null && !firstPlan.getFilePath().isEmpty()) {
                    project.setPlanFilePath(firstPlan.getFilePath());
                }
                if (firstPlan.getFileName() != null && !firstPlan.getFileName().isEmpty()) {
                    project.setPlanFileName(firstPlan.getFileName());
                }
            }
        }
        return project;
    }

    /**
     * 新增项目
     * 使用事务确保项目和成员数据的一致性
     */
    @Override
    @Transactional
    public void add(Project project) {
        // 设置创建时间和默认状态
        project.setCreateTime(LocalDateTime.now());
        if (project.getStatus() == null || project.getStatus().isEmpty()) {
            project.setStatus("draft"); // 草稿状态
        }

        // 插入项目
        projectMapper.insert(project);

        // 插入项目成员
        if (project.getMembers() != null && !project.getMembers().isEmpty()) {
            for (ProjectMember member : project.getMembers()) {
                member.setProjectId(project.getId());
                member.setCreateTime(LocalDateTime.now());
                projectMemberMapper.insert(member);
            }
        }

        // 插入指导老师
        if (project.getAdvisors() != null && !project.getAdvisors().isEmpty()) {
            for (ProjectAdvisor advisor : project.getAdvisors()) {
                advisor.setProjectId(project.getId());
                projectAdvisorMapper.insert(advisor);
            }
        }
        persistPlansAndIndex(project);
    }

    /**
     * 更新项目
     * 使用事务确保项目和成员数据的一致性
     */
    @Override
    @Transactional
    public void update(Project project) {
        // 更新项目信息
        projectMapper.update(project);

        // 先删除原有成员
        if (project.getId() != null) {
            projectMemberMapper.deleteByProjectId(project.getId());
            // 删除原有指导老师
            projectAdvisorMapper.deleteByProjectId(project.getId());
        }

        // 重新插入成员
        if (project.getMembers() != null && !project.getMembers().isEmpty()) {
            for (ProjectMember member : project.getMembers()) {
                member.setProjectId(project.getId());
                member.setCreateTime(LocalDateTime.now());
                projectMemberMapper.insert(member);
            }
        }

        // 重新插入指导老师
        if (project.getAdvisors() != null && !project.getAdvisors().isEmpty()) {
            for (ProjectAdvisor advisor : project.getAdvisors()) {
                advisor.setProjectId(project.getId());
                projectAdvisorMapper.insert(advisor);
            }
        }
        persistPlansAndIndex(project);
    }

    private void persistPlansAndIndex(Project project) {
        if (project.getId() == null) {
            return;
        }
        List<ProjectPlan> plans = project.getPlans();
        if ((plans == null || plans.isEmpty())
                && project.getPlanFilePath() != null && !project.getPlanFilePath().isBlank()) {
            ProjectPlan fromProject = new ProjectPlan();
            fromProject.setProjectId(project.getId());
            fromProject.setFilePath(project.getPlanFilePath());
            fromProject.setFileName(project.getPlanFileName());
            fromProject.setCreateTime(LocalDateTime.now());
            plans = List.of(fromProject);
        }
        if (plans == null) {
            return;
        }
        List<ProjectPlan> existing = projectPlanMapper.listByProjectId(project.getId());
        for (ProjectPlan plan : plans) {
            if (plan.getFilePath() == null || plan.getFilePath().isBlank()) {
                continue;
            }
            plan.setProjectId(project.getId());
            boolean already = existing != null && existing.stream()
                    .anyMatch(p -> plan.getFilePath().equals(p.getFilePath()));
            if (!already) {
                if (plan.getCreateTime() == null) {
                    plan.setCreateTime(LocalDateTime.now());
                }
                projectPlanMapper.insert(plan);
            } else if (plan.getId() == null) {
                plan.setId(existing.stream()
                        .filter(p -> plan.getFilePath().equals(p.getFilePath()))
                        .findFirst()
                        .map(ProjectPlan::getId)
                        .orElse(null));
            }
            if (plan.getId() != null) {
                documentIndexService.indexPlan(project.getId(), plan.getId(), plan.getFilePath(), plan.getFileName());
            }
        }
    }

    /**
     * 删除项目
     * 由于设置了外键级联删除，删除项目时会自动删除关联的成员
     */
    @Override
    @Transactional
    public void delete(Integer id) {
        // 先删除成员（虽然外键会自动删除，但显式删除更清晰）
        projectMemberMapper.deleteByProjectId(id);
        // 删除指导老师
        projectAdvisorMapper.deleteByProjectId(id);
        // 删除项目
        projectMapper.delete(id);
    }

    /**
     * 提交项目申报
     */
    @Override
    @Transactional
    public void submit(Integer id) {
        Project project = projectMapper.getById(id);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        
        // 更新状态为已申报，并设置提交时间
        project.setStatus("submitted");
        project.setSubmitTime(LocalDateTime.now());
        projectMapper.update(project);
    }

    /**
     * 审批项目（通过）
     */
    @Override
    @Transactional
    public void approve(Integer id, String auditorName) {
        Project project = projectMapper.getById(id);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        
        // 更新状态为已审批，并设置审批人
        project.setStatus("approved");
        project.setAuditorName(auditorName);
        projectMapper.update(project);
    }

    /**
     * 驳回项目
     */
    @Override
    @Transactional
    public void reject(Integer id, String auditorName, String rejectReason) {
        Project project = projectMapper.getById(id);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        
        // 更新状态为已拒绝，并设置审批人和驳回原因
        project.setStatus("rejected");
        project.setAuditorName(auditorName);
        project.setRejectReason(rejectReason);
        projectMapper.update(project);
    }
}
