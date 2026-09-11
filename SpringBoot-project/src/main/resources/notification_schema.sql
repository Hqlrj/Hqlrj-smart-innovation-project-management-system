CREATE TABLE IF NOT EXISTS `notification` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL COMMENT '接收人ID',
  `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
  `content` VARCHAR(500) DEFAULT NULL COMMENT '通知内容',
  `type` VARCHAR(50) NOT NULL COMMENT '类型: project_approve, project_reject, award_approve, award_reject, notice_publish',
  `related_id` INT DEFAULT NULL COMMENT '关联业务ID(project_id/award_id/notice_id)',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;