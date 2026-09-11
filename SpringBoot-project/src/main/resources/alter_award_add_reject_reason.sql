-- 为获奖记录表添加未通过说明字段
-- 用于存储审核人驳回时的说明信息

ALTER TABLE `award` 
ADD COLUMN `reject_reason` TEXT COMMENT '未通过原因说明' AFTER `status`;

-- 添加索引，用于快速查询有驳回原因的记录
CREATE INDEX `idx_reject_reason` ON `award`(`reject_reason`(100));
