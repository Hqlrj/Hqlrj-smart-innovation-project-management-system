-- 为用户表添加API Key字段
ALTER TABLE user ADD COLUMN api_key VARCHAR(255) NULL COMMENT 'DeepSeek API Key';