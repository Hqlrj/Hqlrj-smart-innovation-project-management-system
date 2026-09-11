package org.example.springbootproject.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 站内通知实体类
 * 对应数据库中的 notification 表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    private Integer id;            // 通知ID
    private Integer userId;        // 接收人ID
    private String title;          // 通知标题
    private String content;        // 通知内容
    private String type;           // 类型: project_approve / project_reject / award_approve / award_reject / notice_publish
    private Integer relatedId;     // 关联业务ID
    private Integer isRead;        // 是否已读: 0-未读, 1-已读

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime; // 创建时间
}