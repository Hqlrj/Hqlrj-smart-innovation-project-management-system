-- 重新创建ai_review_section表，确保字段类型正确
DROP TABLE IF EXISTS `ai_review_section`;

CREATE TABLE `ai_review_section` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '章节记录ID',
  `task_id` INT NOT NULL COMMENT '任务ID',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '章节顺序',
  `section_title` VARCHAR(255) NOT NULL COMMENT '章节标题',
  `section_level` INT NOT NULL DEFAULT 1 COMMENT '章节层级',
  `char_count` INT NOT NULL DEFAULT 0 COMMENT '正文字符数',
  `score` DECIMAL(4,2) DEFAULT NULL COMMENT '章节得分(0-5)',
  `dimensions` JSON DEFAULT NULL COMMENT '维度评分明细',
  `strengths` TEXT DEFAULT NULL COMMENT '优点',
  `weaknesses` TEXT DEFAULT NULL COMMENT '问题',
  `risks` TEXT DEFAULT NULL COMMENT '风险',
  `suggestions` TEXT DEFAULT NULL COMMENT '修改建议',
  `evidence_unverified_count` INT NOT NULL DEFAULT 0 COMMENT '未核实证据条数',
  `raw_response` MEDIUMTEXT DEFAULT NULL COMMENT '模型原始响应',
  `status` VARCHAR(32) DEFAULT NULL COMMENT 'PENDING/SUCCESS/FAILED',
  `error_msg` VARCHAR(1000) DEFAULT NULL COMMENT '失败原因',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX `idx_air_section_task` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 评审章节结果表';