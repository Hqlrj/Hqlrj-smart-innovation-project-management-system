package org.example.springbootproject.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 公告实体类
 * 对应数据库中的 notice 表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notice {

    private Integer id;              // 公告ID
    private String title;            // 公告标题
    private String content;          // 公告内容
    private Integer publisherId;     // 发布人ID
    private String publisherName;    // 发布人姓名
    private String publisherRole;    // 发布人角色
    private Integer isTop;           // 是否置顶：0-否，1-是
    private String status;           // 状态：normal-正常

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime; // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime; // 更新时间
}
