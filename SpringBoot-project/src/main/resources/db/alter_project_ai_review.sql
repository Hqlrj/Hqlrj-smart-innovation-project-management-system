-- 项目表 AI 评审扩展字段（第 0 步，按需执行一次）
USE bsgl;

ALTER TABLE `project`
  ADD COLUMN `review_mode` VARCHAR(16) DEFAULT NULL COMMENT 'MANUAL/AI_ASSISTED' AFTER `reject_reason`;

ALTER TABLE `project`
  ADD COLUMN `ai_task_id` INT DEFAULT NULL COMMENT '最近一次 AI 任务ID' AFTER `review_mode`;
