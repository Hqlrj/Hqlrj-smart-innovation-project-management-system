-- 项目申报数据库设计
-- 数据库名：bsgl

-- 1. 项目表（project）
CREATE TABLE IF NOT EXISTS `project` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '项目ID',
  `project_type` VARCHAR(50) COMMENT '项目类型',
  `project_name` VARCHAR(200) NOT NULL COMMENT '项目名称',
  `project_intro` TEXT COMMENT '项目简介',
  `innovation_points` TEXT COMMENT '项目创新点',
  `project_background` TEXT COMMENT '项目背景',
  `project_significance` TEXT COMMENT '项目意义',
  `market_demand_analysis` TEXT COMMENT '市场需求分析',
  `key_technology` TEXT COMMENT '项目关键技术介绍',
  `plan_file_path` VARCHAR(500) COMMENT '计划书文件路径',
  `plan_file_name` VARCHAR(200) COMMENT '计划书文件名',
  `status` VARCHAR(20) DEFAULT 'draft' COMMENT '项目状态：draft-草稿, submitted-已申报, approved-已审批, rejected-已拒绝, settled-入驻',
  `applicant_id` INT COMMENT '申请人ID（关联user表）',
  `applicant_name` VARCHAR(50) COMMENT '申请人姓名',
  `auditor_name` VARCHAR(50) COMMENT '审批人姓名',
  `project_space` VARCHAR(50) COMMENT '项目空间（暂时空着）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `submit_time` DATETIME COMMENT '提交时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- 2. 项目成员表（project_member）
CREATE TABLE IF NOT EXISTS `project_member` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '成员ID',
  `project_id` INT NOT NULL COMMENT '项目ID（关联project表）',
  `student_id` VARCHAR(50) NOT NULL COMMENT '学号',
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `gender` VARCHAR(10) COMMENT '性别：男、女',
  `phone` VARCHAR(20) COMMENT '手机号',
  `college` VARCHAR(100) COMMENT '所属学院',
  `class_name` VARCHAR(100) COMMENT '班级',
  `introduction` TEXT COMMENT '介绍',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_project_id` (`project_id`),
  INDEX `idx_student_id` (`student_id`),
  FOREIGN KEY (`project_id`) REFERENCES `project`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目成员表';

-- 3. 索引创建
-- 项目表索引
CREATE INDEX `idx_project_type` ON `project`(`project_type`);
CREATE INDEX `idx_project_name` ON `project`(`project_name`);
CREATE INDEX `idx_status` ON `project`(`status`);
CREATE INDEX `idx_applicant_id` ON `project`(`applicant_id`);
CREATE INDEX `idx_create_time` ON `project`(`create_time`);

-- 项目成员表索引（已在表定义中创建）
-- idx_project_id: 用于快速查询某个项目的所有成员
-- idx_student_id: 用于快速查询某个学生参与的所有
-- 项目

-- 4. 获奖记录表（award）
CREATE TABLE IF NOT EXISTS `award` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '获奖记录ID',
  `project_id` INT NOT NULL COMMENT '项目ID（关联project表）',
  `project_name` VARCHAR(200) COMMENT '项目名称',
  `competition_name` VARCHAR(200) NOT NULL COMMENT '比赛名称',
  `competition_level` VARCHAR(50) COMMENT '比赛级别：校级、省级、国家级等',
  `award_level` VARCHAR(50) COMMENT '获奖等级：一等奖、二等奖、三等奖等',
  `award_certificate` VARCHAR(500) COMMENT '获奖证明（图片路径）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_project_id` (`project_id`),
  FOREIGN KEY (`project_id`) REFERENCES `project`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='获奖记录表';

-- 5. 公告表（notice）
CREATE TABLE IF NOT EXISTS `notice` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '公告ID',
  `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
  `content` TEXT NOT NULL COMMENT '公告内容',
  `publisher_id` INT NOT NULL COMMENT '发布人ID（关联user表）',
  `publisher_name` VARCHAR(50) COMMENT '发布人姓名',
  `publisher_role` VARCHAR(50) COMMENT '发布人角色',
  `is_top` TINYINT(1) DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
  `status` VARCHAR(20) DEFAULT 'normal' COMMENT '状态：normal-正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_notice_create_time` (`create_time`),
  INDEX `idx_notice_publisher_id` (`publisher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';
