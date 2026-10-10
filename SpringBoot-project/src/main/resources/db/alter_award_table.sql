-- 为获奖记录表添加状态字段
-- 状态：pending-未审批, approved-已审批通过, rejected-已审批未通过

ALTER TABLE `award` 
ADD COLUMN `status` VARCHAR(20) DEFAULT 'pending' COMMENT '审批状态：pending-未审批, approved-已审批通过, rejected-已审批未通过' AFTER `award_certificate`;

-- 添加状态索引，用于快速查询
CREATE INDEX `idx_status` ON `award`(`status`);

