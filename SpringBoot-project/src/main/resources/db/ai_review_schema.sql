-- AI 评审模块数据库脚本（第 0 步）
-- 使用前请确认已连接 bsgl 库：USE bsgl;

-- 1. 评审任务主表
CREATE TABLE IF NOT EXISTS `ai_review_task` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
  `project_id` INT NOT NULL COMMENT '项目ID',
  `plan_id` INT NOT NULL COMMENT '计划书ID',
  `status` VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PARSING/REVIEWING/SUMMARIZING/DONE/FAILED',
  `review_mode` VARCHAR(16) NOT NULL DEFAULT 'AI_ASSISTED' COMMENT 'MANUAL/AI_ASSISTED',
  `section_total` INT NOT NULL DEFAULT 0 COMMENT '章节总数',
  `section_done` INT NOT NULL DEFAULT 0 COMMENT '已完成章节数',
  `total_score` DECIMAL(5,2) DEFAULT NULL COMMENT '综合得分(0-100)',
  `grade` VARCHAR(8) DEFAULT NULL COMMENT '等级 A/B/C/D',
  `file_hash` VARCHAR(64) DEFAULT NULL COMMENT '计划书 SHA-256',
  `model_name` VARCHAR(64) DEFAULT NULL COMMENT '模型名称',
  `prompt_version` VARCHAR(32) DEFAULT NULL COMMENT '提示词版本',
  `token_input` INT NOT NULL DEFAULT 0 COMMENT '输入 token',
  `token_output` INT NOT NULL DEFAULT 0 COMMENT '输出 token',
  `duration_ms` BIGINT NOT NULL DEFAULT 0 COMMENT '总耗时毫秒',
  `error_msg` VARCHAR(1000) DEFAULT NULL COMMENT '失败原因',
  `created_by` INT DEFAULT NULL COMMENT '发起人用户ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  INDEX `idx_air_task_project` (`project_id`),
  INDEX `idx_air_task_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 评审任务表';

-- 2. 章节切分与评审结果（第 4-7 步使用，第 0 步先建表）
CREATE TABLE IF NOT EXISTS `ai_review_section` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '章节记录ID',
  `task_id` INT NOT NULL COMMENT '任务ID',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '章节顺序',
  `section_title` VARCHAR(255) NOT NULL COMMENT '章节标题',
  `section_level` INT NOT NULL DEFAULT 1 COMMENT '章节层级',
  `char_count` INT NOT NULL DEFAULT 0 COMMENT '正文字符数',
  `score` DECIMAL(4,2) DEFAULT NULL COMMENT '章节得分(0-5)',
  `dimensions` JSON DEFAULT NULL COMMENT '维度评分明细',
  `strengths` JSON DEFAULT NULL COMMENT '优点',
  `weaknesses` JSON DEFAULT NULL COMMENT '问题',
  `risks` JSON DEFAULT NULL COMMENT '风险',
  `suggestions` JSON DEFAULT NULL COMMENT '修改建议',
  `evidence_unverified_count` INT NOT NULL DEFAULT 0 COMMENT '未核实证据条数',
  `raw_response` MEDIUMTEXT DEFAULT NULL COMMENT '模型原始响应',
  `status` VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS/FAILED',
  `error_msg` VARCHAR(1000) DEFAULT NULL COMMENT '失败原因',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX `idx_air_section_task` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 评审章节结果表';

-- 3. 总体报告（第 8 步使用）
CREATE TABLE IF NOT EXISTS `ai_review_report` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '报告ID',
  `task_id` INT NOT NULL COMMENT '任务ID',
  `total_score` DECIMAL(5,2) DEFAULT NULL COMMENT '综合得分',
  `grade` VARCHAR(8) DEFAULT NULL COMMENT '等级',
  `summary` TEXT DEFAULT NULL COMMENT '总体评述',
  `highlights` JSON DEFAULT NULL COMMENT '亮点',
  `main_problems` JSON DEFAULT NULL COMMENT '主要问题',
  `risks` JSON DEFAULT NULL COMMENT '风险',
  `suggestions` JSON DEFAULT NULL COMMENT '修改建议',
  `missing_sections` JSON DEFAULT NULL COMMENT '缺失章节',
  `ai_recommendation` VARCHAR(32) DEFAULT NULL COMMENT 'AI 参考建议',
  `consistency_note` VARCHAR(500) DEFAULT NULL COMMENT '一致性说明',
  `raw_json` MEDIUMTEXT DEFAULT NULL COMMENT '原始 JSON',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY `uk_air_report_task` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 评审总体报告表';

-- 4. 人工复核结论（第 9 步使用）
CREATE TABLE IF NOT EXISTS `human_review` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '人工复核ID',
  `task_id` INT NOT NULL COMMENT '任务ID',
  `project_id` INT NOT NULL COMMENT '项目ID',
  `reviewer_id` INT NOT NULL COMMENT '复核人ID',
  `reviewer_name` VARCHAR(64) NOT NULL COMMENT '复核人姓名',
  `ai_score_snapshot` DECIMAL(5,2) DEFAULT NULL COMMENT 'AI 原始分快照',
  `human_decision` VARCHAR(16) NOT NULL COMMENT 'PASS/REJECT',
  `comment` TEXT DEFAULT NULL COMMENT '复核意见',
  `is_overridden` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否推翻 AI 结论',
  `review_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '复核时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX `idx_human_review_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人工复核表';

-- 5. 项目表扩展字段（若列已存在可忽略报错）
-- ALTER TABLE `project` ADD COLUMN `review_mode` VARCHAR(16) DEFAULT NULL COMMENT 'MANUAL/AI_ASSISTED' AFTER `reject_reason`;
-- ALTER TABLE `project` ADD COLUMN `ai_task_id` INT DEFAULT NULL COMMENT '最近一次 AI 任务ID' AFTER `review_mode`;
