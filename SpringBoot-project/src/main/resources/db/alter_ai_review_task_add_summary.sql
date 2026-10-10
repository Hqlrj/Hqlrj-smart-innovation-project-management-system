-- 为 ai_review_task 表添加 summary 字段用于存储总体评价
ALTER TABLE `ai_review_task` ADD COLUMN `summary` TEXT DEFAULT NULL COMMENT '总体评价' AFTER `grade`;