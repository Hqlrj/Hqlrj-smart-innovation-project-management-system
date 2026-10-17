CREATE TABLE IF NOT EXISTS `ai_vector_chunk` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `source_type` VARCHAR(30) NOT NULL COMMENT 'notice / plan',
  `source_id` INT NOT NULL COMMENT '公告ID或计划书ID',
  `project_id` INT DEFAULT NULL COMMENT '关联项目ID',
  `college` VARCHAR(100) DEFAULT NULL COMMENT '学院，空表示全校可见',
  `title` VARCHAR(300) DEFAULT NULL,
  `content` TEXT NOT NULL,
  `embedding` MEDIUMTEXT NOT NULL COMMENT '逗号分隔的向量',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_vec_source` (`source_type`, `source_id`),
  INDEX `idx_vec_college` (`college`),
  INDEX `idx_vec_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档向量库';
