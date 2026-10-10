-- 修改ai_review_task表，让必填字段可以为null
-- 执行此SQL后，AI审核功能才能正常工作

ALTER TABLE ai_review_task MODIFY COLUMN plan_id INT NULL;
ALTER TABLE ai_review_task MODIFY COLUMN review_mode VARCHAR(50) NULL;
ALTER TABLE ai_review_task MODIFY COLUMN section_total INT NULL;
ALTER TABLE ai_review_task MODIFY COLUMN section_done INT NULL;
ALTER TABLE ai_review_task MODIFY COLUMN created_by INT NULL;