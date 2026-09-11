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
import org.example.springbootproject.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * 分页查询项目列表
     */
    @Override
    public PageBean<Project> list(Integer page, Integer pageSize, String projectName, String projectType, String status, String projectSpace, Integer applicantId) {
        // 设置分页参数
        PageHelper.startPage(page, pageSize);

        // 执行查询
        List<Project> projectList = projectMapper.list(projectName, projectType, status, projectSpace, applicantId);

        // 获取分页信息
        Page<Project> p = (Page<Project>) projectList;

        // 封装分页结果
        return PageBean.of(p.getTotal(), p.getResult());
    }

    /**
     * 根据ID查询项目（包含成员列表、计划书和获奖记录）
     */
    @Override
    public Project getById(Integer id) {
        Project project = projectMapper.getById(id);
        if (project != null) {
            // 查询项目成员列表
            List<ProjectMember> members = projectMemberMapper.listByProjectId(id);
            project.setMembers(members);
            
            // 查询指导老师列表
            List<ProjectAdvisor> advisors = projectAdvisorMapper.listByProjectId(id);
            project.setAdvisors(advisors);
            
            // 查询获奖记录列表（只显示已审批通过的记录）
            List<Award> allAwards = awardMapper.listByProjectId(id);
            // 过滤出状态为"approved"的记录
            List<Award> approvedAwards = allAwards.stream()
                    .filter(award -> "approved".equals(award.getStatus()))
                    .collect(Collectors.toList());
            project.setAwards(approvedAwards);
            
            // 查询计划书列表
            List<ProjectPlan> plans = projectPlanMapper.listByProjectId(id);
            
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
