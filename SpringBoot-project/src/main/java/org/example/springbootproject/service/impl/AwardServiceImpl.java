package org.example.springbootproject.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import org.example.springbootproject.mapper.AwardMapper;
import org.example.springbootproject.pojo.Award;
import org.example.springbootproject.pojo.PageBean;
import org.example.springbootproject.service.AwardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 获奖记录服务实现类
 */
@Service
public class AwardServiceImpl implements AwardService {

    @Autowired
    private AwardMapper awardMapper;

    @Override
    public PageBean<Award> list(Integer page, Integer pageSize, String projectName,
                                 String competitionName, String competitionLevel,
                                 String awardLevel, Integer applicantId) {
        // 设置分页参数
        PageHelper.startPage(page, pageSize);

        // 执行查询
        List<Award> awardList = awardMapper.list(projectName, competitionName, competitionLevel, awardLevel, applicantId);

        // 获取分页信息
        Page<Award> p = (Page<Award>) awardList;

        // 封装分页结果
        return PageBean.of(p.getTotal(), p.getResult());
    }

    @Override
    public List<Award> listByProjectId(Integer projectId) {
        // 查询所有获奖记录
        List<Award> allAwards = awardMapper.listByProjectId(projectId);
        // 只返回已审批通过的记录（用于项目详情展示）
        return allAwards.stream()
                .filter(award -> "approved".equals(award.getStatus()))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Award getById(Integer id) {
        return awardMapper.getById(id);
    }

    @Override
    public void add(Award award) {
        // 如果没有设置状态，默认为未审批
        if (award.getStatus() == null || award.getStatus().isEmpty()) {
            award.setStatus("pending");
        }
        award.setCreateTime(LocalDateTime.now());
        awardMapper.insert(award);
    }

    @Override
    public void update(Award award) {
        awardMapper.update(award);
    }

    @Override
    public void delete(Integer id) {
        awardMapper.delete(id);
    }

    @Override
    public void approve(Integer id) {
        Award award = awardMapper.getById(id);
        if (award != null) {
            award.setStatus("approved");
            awardMapper.update(award);
        }
    }

    @Override
    public void reject(Integer id, String reason) {
        Award award = awardMapper.getById(id);
        if (award != null) {
            award.setStatus("rejected");
            award.setRejectReason(reason);
            awardMapper.update(award);
        }
    }
}
