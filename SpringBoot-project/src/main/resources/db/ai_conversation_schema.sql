-- AI对话历史表
CREATE TABLE ai_conversation (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL COMMENT '用户ID',
    conversation_id VARCHAR(64) NOT NULL COMMENT '对话ID',
    title VARCHAR(255) COMMENT '对话标题',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_message_time DATETIME COMMENT '最后消息时间',
    INDEX idx_user_id (user_id),
    INDEX idx_conversation_id (conversation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话历史表';

-- AI对话消息表
CREATE TABLE ai_message (
    id INT AUTO_INCREMENT PRIMARY KEY,
    conversation_id VARCHAR(64) NOT NULL COMMENT '对话ID',
    role ENUM('user', 'assistant') NOT NULL COMMENT '角色',
    content TEXT NOT NULL COMMENT '消息内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话消息表';