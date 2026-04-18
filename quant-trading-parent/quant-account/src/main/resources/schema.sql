-- 账户模块数据库表结构
-- 数据库: quant_trading

-- 客户信息表
CREATE TABLE IF NOT EXISTS `client` (
    `client_id` VARCHAR(5) PRIMARY KEY COMMENT '客户编号',
    `client_name` VARCHAR(50) NOT NULL COMMENT '客户名称',
    `open_date` VARCHAR(8) NOT NULL COMMENT '开户日期',
    `cancel_date` VARCHAR(8) DEFAULT NULL COMMENT '销户日期',
    `password` VARCHAR(100) NOT NULL COMMENT '登录密码',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户信息表';

-- 资金表
CREATE TABLE IF NOT EXISTS `fund` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `client_id` VARCHAR(5) NOT NULL COMMENT '客户编号',
    `client_name` VARCHAR(50) NOT NULL COMMENT '客户名称',
    `fund_account` VARCHAR(10) NOT NULL UNIQUE COMMENT '资产账号',
    `begin_balance` DECIMAL(18,2) DEFAULT 0.00 COMMENT '期初金额',
    `current_balance` DECIMAL(18,2) DEFAULT 0.00 COMMENT '当前金额',
    `asset_prop` VARCHAR(10) DEFAULT '现金' COMMENT '资产属性',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_client_id` (`client_id`),
    INDEX `idx_fund_account` (`fund_account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资金表';

-- 股东账户表
CREATE TABLE IF NOT EXISTS `stockholder` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `client_id` VARCHAR(5) NOT NULL COMMENT '客户编号',
    `client_name` VARCHAR(50) NOT NULL COMMENT '客户名称',
    `fund_account` VARCHAR(10) NOT NULL COMMENT '资产账号',
    `stock_account` VARCHAR(5) NOT NULL COMMENT '证券账号',
    `exchange_type` TINYINT NOT NULL COMMENT '交易市场(0:上海,1:深圳,3:北京)',
    `open_date` VARCHAR(8) NOT NULL COMMENT '开户日期',
    `cancel_date` VARCHAR(8) DEFAULT NULL COMMENT '销户日期',
    `holder_kind` TINYINT DEFAULT 0 COMMENT '账号类别(0:普通账户,1:基金账户)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_client_exchange` (`client_id`, `exchange_type`),
    INDEX `idx_stock_account` (`stock_account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股东账户表';

-- 股份表
CREATE TABLE IF NOT EXISTS `stock` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `client_id` VARCHAR(5) NOT NULL COMMENT '客户编号',
    `fund_account` VARCHAR(10) NOT NULL COMMENT '资产账号',
    `exchange_type` TINYINT NOT NULL COMMENT '交易市场',
    `stock_account` VARCHAR(5) NOT NULL COMMENT '证券账号',
    `stock_code` VARCHAR(10) NOT NULL COMMENT '证券代码',
    `stock_name` VARCHAR(50) DEFAULT NULL COMMENT '证券名称',
    `stock_type` VARCHAR(10) DEFAULT '股票' COMMENT '证券类别',
    `begin_amount` DECIMAL(18,4) DEFAULT 0 COMMENT '期初数量',
    `current_amount` DECIMAL(18,4) DEFAULT 0 COMMENT '当前数量',
    `sum_buy_amount` DECIMAL(18,4) DEFAULT 0 COMMENT '累计买入数量',
    `sum_buy_balance` DECIMAL(18,2) DEFAULT 0 COMMENT '累计买入金额',
    `sum_sell_amount` DECIMAL(18,4) DEFAULT 0 COMMENT '累计卖出数量',
    `sum_sell_balance` DECIMAL(18,2) DEFAULT 0 COMMENT '累计卖出金额',
    `cost_price` DECIMAL(18,4) DEFAULT 0 COMMENT '成本价',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_client_id` (`client_id`),
    INDEX `idx_fund_account` (`fund_account`),
    INDEX `idx_stock_account` (`stock_account`),
    INDEX `idx_stock_code` (`stock_code`),
    UNIQUE KEY `uk_account_stock` (`fund_account`, `stock_account`, `stock_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股份表';

-- 初始化测试数据
-- 测试客户1
INSERT INTO `client` (`client_id`, `client_name`, `open_date`, `password`) VALUES ('00001', '张三', '20260328', '123456');

INSERT INTO `fund` (`client_id`, `client_name`, `fund_account`, `begin_balance`, `current_balance`, `asset_prop`)
VALUES ('00001', '张三', '80000001', '1000000.00', '850000.00', '现金');

INSERT INTO `stockholder` (`client_id`, `client_name`, `fund_account`, `stock_account`, `exchange_type`, `open_date`, `holder_kind`)
VALUES
('00001', '张三', '80000001', '00001', 0, '20260328', 0),
('00001', '张三', '80000001', '10001', 1, '20260328', 0);

-- 测试客户2
INSERT INTO `client` (`client_id`, `client_name`, `open_date`, `password`) VALUES ('00002', '李四', '20260328', '123456');

INSERT INTO `fund` (`client_id`, `client_name`, `fund_account`, `begin_balance`, `current_balance`, `asset_prop`)
VALUES ('00002', '李四', '80000002', '500000.00', '520000.00', '现金');

INSERT INTO `stockholder` (`client_id`, `client_name`, `fund_account`, `stock_account`, `exchange_type`, `open_date`, `holder_kind`)
VALUES
('00002', '李四', '80000002', '00002', 0, '20260328', 0),
('00002', '李四', '80000002', '10002', 1, '20260328', 0);
