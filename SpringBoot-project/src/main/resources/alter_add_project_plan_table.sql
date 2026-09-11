-- 为现有数据库添加计划书表
CREATE TABLE IF NOT EXISTS `project_plan` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '计划书ID',
  `project_id` INT NOT NULL COMMENT '项目ID（关联project表）',
  `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径',
  `file_name` VARCHAR(200) NOT NULL COMMENT '文件名',
  `file_size` BIGINT COMMENT '文件大小（字节）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_project_id` (`project_id`),
  FOREIGN KEY (`project_id`) REFERENCES `project`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计划书表';

