-- 为 project_member 表添加 gender 字段
ALTER TABLE `project_member` 
ADD COLUMN `gender` VARCHAR(10) COMMENT '性别：男、女' AFTER `name`;

