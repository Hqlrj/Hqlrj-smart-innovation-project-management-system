package org.example.springbootproject.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.example.springbootproject.mapper.NoticeMapper;
import org.example.springbootproject.pojo.Notice;
import org.example.springbootproject.pojo.PageBean;
import org.example.springbootproject.service.DocumentIndexService;
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

    @Autowired
    private DocumentIndexService documentIndexService;

    @Override
    public PageBean<Notice> list(Integer page, Integer pageSize, String title, String viewerCollege,
                                 Integer userId, String readStatus) {
        PageHelper.startPage(page, pageSize);
        List<Notice> list = noticeMapper.list(title, viewerCollege, userId, readStatus);
        // PageHelper 返回的实现通常是 com.github.pagehelper.Page
        Page<Notice> p = (Page<Notice>) list;
        return PageBean.of(p.getTotal(), p.getResult());
    }

    @Override
    public Notice getById(Integer id, Integer userId) {
        return noticeMapper.getById(id, userId);
    }

    @Override
    public void markRead(Integer noticeId, Integer userId) {
        if (noticeId == null || userId == null) {
            return;
        }
        noticeMapper.markRead(noticeId, userId);
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
        // 发布人自己发布的公告默认已读，不占待查看列表
        if (notice.getId() != null && notice.getPublisherId() != null) {
            noticeMapper.markRead(notice.getId(), notice.getPublisherId());
        }
        documentIndexService.indexNotice(notice);
    }

    @Override
    public void delete(Integer id) {
        documentIndexService.deleteNotice(id);
        noticeMapper.deleteReadsByNoticeId(id);
        noticeMapper.delete(id);
    }
}
