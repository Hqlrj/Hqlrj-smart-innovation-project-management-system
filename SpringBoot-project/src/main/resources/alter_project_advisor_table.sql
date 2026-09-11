-- 创建项目指导老师表
-- 执行前请先确认数据库已存在

CREATE TABLE IF NOT EXISTS `project_advisor` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '指导老师ID',
  `project_id` INT NOT NULL COMMENT '项目ID（关联project表）',
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `gender` VARCHAR(10) COMMENT '性别：男、女',
  `phone` VARCHAR(20) COMMENT '手机号',
  `college` VARCHAR(100) COMMENT '所属学院',
  `introduction` TEXT COMMENT '介绍',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_project_id` (`project_id`),
  FOREIGN KEY (`project_id`) REFERENCES `project`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目指导老师表';

