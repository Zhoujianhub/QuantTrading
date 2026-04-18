-- 通知渠道表
CREATE TABLE IF NOT EXISTS `notification_channel` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `account_id` VARCHAR(64) NOT NULL COMMENT '账户ID',
  `channel_name` VARCHAR(128) NOT NULL COMMENT '渠道名称',
  `channel_type` VARCHAR(32) NOT NULL COMMENT '渠道类型：CONSOLE/EMAIL/WEBHOOK/WECHAT_WORK/DINGTALK',
  `channel_config` TEXT NOT NULL COMMENT '渠道配置（JSON格式）',
  `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_account_id` (`account_id`),
  KEY `idx_channel_type` (`channel_type`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知渠道表';

-- 通知记录表
CREATE TABLE IF NOT EXISTS `notification_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `account_id` VARCHAR(64) NOT NULL COMMENT '账户ID',
  `notification_type` VARCHAR(32) NOT NULL COMMENT '通知类型',
  `channel_type` VARCHAR(32) NOT NULL COMMENT '渠道类型',
  `title` VARCHAR(256) NOT NULL COMMENT '通知标题',
  `content` TEXT NOT NULL COMMENT '通知内容',
  `recipient` VARCHAR(256) DEFAULT NULL COMMENT '接收人',
  `status` VARCHAR(16) NOT NULL COMMENT '发送状态：PENDING/SUCCESS/FAILED',
  `error_message` VARCHAR(512) DEFAULT NULL COMMENT '错误信息',
  `send_time` DATETIME DEFAULT NULL COMMENT '发送时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_account_id` (`account_id`),
  KEY `idx_notification_type` (`notification_type`),
  KEY `idx_status` (`status`),
  KEY `idx_send_time` (`send_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知记录表';
