-- 为AI评审任务表添加用户ID和模式字段
ALTER TABLE `ai_review_task` 
ADD COLUMN `user_id` INT DEFAULT NULL COMMENT '发起人用户ID' AFTER `created_by`,
ADD COLUMN `mode` VARCHAR(16) DEFAULT NULL COMMENT 'MANUAL/AI' AFTER `user_id`;
