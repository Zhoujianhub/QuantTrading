-- 监控规则表
CREATE TABLE IF NOT EXISTS `monitor_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `account_id` VARCHAR(64) NOT NULL COMMENT '账户ID',
  `rule_name` VARCHAR(128) NOT NULL COMMENT '规则名称',
  `rule_type` VARCHAR(32) NOT NULL COMMENT '规则类型：PROFIT_RATE/TODAY_CHANGE_RATE',
  `threshold_value` DECIMAL(12,4) NOT NULL COMMENT '阈值',
  `condition_type` VARCHAR(16) NOT NULL COMMENT '条件类型：GT/GTE/LT/LTE/EQ',
  `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
  `notify_enabled` TINYINT(1) DEFAULT 1 COMMENT '是否发送通知：0-不通知，1-通知',
  `description` VARCHAR(256) DEFAULT NULL COMMENT '规则描述',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_account_id` (`account_id`),
  KEY `idx_rule_type` (`rule_type`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监控规则表';

-- 监控事件表
CREATE TABLE IF NOT EXISTS `monitor_event` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `rule_id` BIGINT NOT NULL COMMENT '规则ID',
  `account_id` VARCHAR(64) NOT NULL COMMENT '账户ID',
  `asset_code` VARCHAR(32) NOT NULL COMMENT '资产代码',
  `asset_name` VARCHAR(128) DEFAULT NULL COMMENT '资产名称',
  `rule_type` VARCHAR(32) NOT NULL COMMENT '规则类型',
  `trigger_value` DECIMAL(12,4) NOT NULL COMMENT '触发时的值',
  `threshold_value` DECIMAL(12,4) NOT NULL COMMENT '阈值',
  `event_status` VARCHAR(16) DEFAULT 'TRIGGERED' COMMENT '事件状态：TRIGGERED/NOTIFIED/IGNORED',
  `notified_at` DATETIME DEFAULT NULL COMMENT '通知时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_rule_id` (`rule_id`),
  KEY `idx_account_id` (`account_id`),
  KEY `idx_asset_code` (`asset_code`),
  KEY `idx_event_status` (`event_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监控事件表';

-- 插入示例规则：收益率大于10%告警
INSERT INTO `monitor_rule` (`account_id`, `rule_name`, `rule_type`, `threshold_value`, `condition_type`, `enabled`, `notify_enabled`, `description`) VALUES
('TEST001', '收益率超过10%告警', 'PROFIT_RATE', 10.0000, 'GT', 1, 1, '当持仓收益率大于10%时发送通知');
