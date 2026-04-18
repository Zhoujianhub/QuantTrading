-- 量化交易监控系统 - 数据库初始化脚本
-- 创建日期: 2026-03-21

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS quant_trading DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE quant_trading;

-- 持仓表
CREATE TABLE IF NOT EXISTS `holding` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `account_id` VARCHAR(64) NOT NULL COMMENT '账户ID',
  `asset_code` VARCHAR(32) NOT NULL COMMENT '资产代码',
  `asset_name` VARCHAR(128) NOT NULL COMMENT '资产名称',
  `asset_type` VARCHAR(32) DEFAULT NULL COMMENT '资产类型：ETF/LOF/场外基金/股票',
  `trading_type` VARCHAR(16) DEFAULT NULL COMMENT '交易类型：场内/场外',
  `initial_fund` DECIMAL(16,4) NOT NULL COMMENT '初始投入金额',
  `opened_date` DATE DEFAULT NULL COMMENT '建仓日期',
  `initial_nav` DECIMAL(12,4) DEFAULT NULL COMMENT '初始净值/价格',
  `cost_price` DECIMAL(12,4) DEFAULT NULL COMMENT '持仓成本价',
  `current_nav` DECIMAL(12,4) DEFAULT NULL COMMENT '当前净值/价格',
  `current_position_amount` DECIMAL(16,4) DEFAULT NULL COMMENT '当前持仓总金额',
  `holding_shares` DECIMAL(16,4) DEFAULT NULL COMMENT '持有份额',
  `holding_days` INT DEFAULT NULL COMMENT '持仓天数',
  `today_profit` DECIMAL(16,4) DEFAULT NULL COMMENT '今日收益',
  `today_change_rate` DECIMAL(10,4) DEFAULT NULL COMMENT '今日涨跌幅(%)',
  `total_profit` DECIMAL(16,4) DEFAULT NULL COMMENT '持仓收益',
  `total_profit_rate` DECIMAL(10,4) DEFAULT NULL COMMENT '持仓收益率(%)',
  `current_date` DATE DEFAULT NULL COMMENT '当前日期',
  `nav_update_time` DATETIME DEFAULT NULL COMMENT '净值更新时间',
  `import_time` DATETIME DEFAULT NULL COMMENT '导入时间',
  `source_type` VARCHAR(32) DEFAULT NULL COMMENT '数据来源',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_account_id` (`account_id`),
  KEY `idx_asset_code` (`asset_code`),
  KEY `idx_current_date` (`current_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='持仓表';

-- 基金净值历史表
CREATE TABLE IF NOT EXISTS `fund_nav_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `asset_code` VARCHAR(32) NOT NULL COMMENT '资产代码',
  `nav_date` DATE NOT NULL COMMENT '净值日期',
  `unit_nav` DECIMAL(12,4) DEFAULT NULL COMMENT '单位净值',
  `accum_nav` DECIMAL(12,4) DEFAULT NULL COMMENT '累计净值',
  `daily_change_rate` DECIMAL(10,4) DEFAULT NULL COMMENT '日涨跌幅(%)',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_asset_code` (`asset_code`),
  KEY `idx_nav_date` (`nav_date`),
  UNIQUE KEY `uk_asset_nav_date` (`asset_code`, `nav_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基金净值历史表';

-- ETF价格历史表
CREATE TABLE IF NOT EXISTS `etf_price_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `asset_code` VARCHAR(32) NOT NULL COMMENT '资产代码',
  `exchange_code` VARCHAR(16) DEFAULT NULL COMMENT '交易所代码',
  `price` DECIMAL(12,4) NOT NULL COMMENT '价格',
  `change_rate` DECIMAL(10,4) DEFAULT NULL COMMENT '涨跌幅(%)',
  `change_amount` DECIMAL(12,4) DEFAULT NULL COMMENT '涨跌额',
  `volume` BIGINT DEFAULT NULL COMMENT '成交量',
  `amount` DECIMAL(20,4) DEFAULT NULL COMMENT '成交额',
  `price_time` DATETIME NOT NULL COMMENT '价格时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_asset_code` (`asset_code`),
  KEY `idx_price_time` (`price_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ETF价格历史表';

-- 插入测试数据
INSERT INTO `holding` (`account_id`, `asset_code`, `asset_name`, `asset_type`, `trading_type`, `initial_fund`, `opened_date`, `current_date`, `source_type`) VALUES
('TEST001', '000001', '平安沪深300ETF', 'ETF', '场内', 10000.0000, '2026-01-15', CURDATE(), 'IMPORT'),
('TEST001', '510300', '华泰柏瑞沪深300ETF', 'ETF', '场内', 15000.0000, '2026-02-01', CURDATE(), 'IMPORT'),
('TEST001', '000961', '天弘沪深300ETF联接A', '场外基金', '场外', 20000.0000, '2026-01-20', CURDATE(), 'IMPORT');
