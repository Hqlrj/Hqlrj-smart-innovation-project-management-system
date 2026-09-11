package org.example.springbootproject.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.example.springbootproject.mapper.NoticeMapper;
import org.example.springbootproject.pojo.Notice;
import org.example.springbootproject.pojo.PageBean;
import org.example.springbootproject.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告服务实现类
 */
@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    @Override
    public PageBean<Notice> list(Integer page, Integer pageSize, String title) {
        PageHelper.startPage(page, pageSize);
        List<Notice> list = noticeMapper.list(title);
        // PageHelper 返回的实现通常是 com.github.pagehelper.Page
        Page<Notice> p = (Page<Notice>) list;
        return PageBean.of(p.getTotal(), p.getResult());
    }

    @Override
    public Notice getById(Integer id) {
        return noticeMapper.getById(id);
    }

    @Override
    public void add(Notice notice) {
        notice.setCreateTime(LocalDateTime.now());
        if (notice.getIsTop() == null) {
            notice.setIsTop(0);
        }
        if (notice.getStatus() == null || notice.getStatus().isEmpty()) {
            notice.setStatus("normal");
        }
        noticeMapper.insert(notice);
    }

    @Override
    public void delete(Integer id) {
        noticeMapper.delete(id);
    }
}
