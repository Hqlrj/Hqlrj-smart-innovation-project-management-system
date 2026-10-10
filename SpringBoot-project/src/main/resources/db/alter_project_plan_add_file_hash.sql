-- 为 project_plan 表添加 file_hash 字段
ALTER TABLE project_plan ADD COLUMN file_hash VARCHAR(64) DEFAULT NULL COMMENT '文件SHA-256哈希值' AFTER file_size;
