-- 为用户表添加头像字段
ALTER TABLE `user` ADD COLUMN `avatar` VARCHAR(255) NULL COMMENT '头像路径' AFTER `college`;
