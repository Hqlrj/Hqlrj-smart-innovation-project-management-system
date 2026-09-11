-- 为已存在的 project 表添加缺失的字段
-- 执行前请先检查字段是否已存在

-- 检查并添加 project_space 字段
-- 方法1：如果 MySQL 8.0.19+ 支持 IF NOT EXISTS
-- ALTER TABLE `project` 
-- ADD COLUMN IF NOT EXISTS `project_space` VARCHAR(50) COMMENT '项目空间（暂时空着）' AFTER `auditor_name`;

-- 方法2：兼容所有 MySQL 版本（如果字段已存在会报错，可以忽略）
ALTER TABLE `project` 
ADD COLUMN `project_space` VARCHAR(50) COMMENT '项目空间（暂时空着）' AFTER `auditor_name`;

-- 如果执行报错说字段已存在，说明字段已经添加成功，可以忽略错误
