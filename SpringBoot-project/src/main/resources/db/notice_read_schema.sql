-- 公告已读记录：每人独立，看过一次后从「待查看」列表消失，避免置顶公告长期挡住普通公告
CREATE TABLE IF NOT EXISTS `notice_read` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
  `notice_id` INT NOT NULL COMMENT '公告ID',
  `user_id` INT NOT NULL COMMENT '阅读人ID',
  `read_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  UNIQUE KEY `uk_notice_user` (`notice_id`, `user_id`),
  INDEX `idx_notice_read_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告已读记录';
