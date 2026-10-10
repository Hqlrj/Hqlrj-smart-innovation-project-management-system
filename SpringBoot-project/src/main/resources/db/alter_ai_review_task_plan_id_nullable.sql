-- 修改ai_review_task表，让plan_id可以为null
ALTER TABLE ai_review_task MODIFY COLUMN plan_id INT NULL;