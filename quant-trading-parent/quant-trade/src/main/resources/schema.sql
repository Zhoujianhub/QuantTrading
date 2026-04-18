-- 交易模块数据库表结构
-- 数据库: quant_trading

-- 委托表
CREATE TABLE IF NOT EXISTS `entrust` (
    `entrust_no` VARCHAR(32) PRIMARY KEY COMMENT '委托编号',
    `init_date` DATE NOT NULL COMMENT '委托日期',
    `curr_date` DATE NOT NULL COMMENT '当前日期',
    `client_id` VARCHAR(5) NOT NULL COMMENT '客户编号',
    `fund_account` VARCHAR(10) NOT NULL COMMENT '资产账号',
    `exchange_type` TINYINT NOT NULL COMMENT '交易市场(0:上海,1:深圳,3:北京)',
    `stock_account` VARCHAR(10) NOT NULL COMMENT '证券账号',
    `stock_code` VARCHAR(10) NOT NULL COMMENT '证券代码',
    `stock_name` VARCHAR(50) DEFAULT NULL COMMENT '证券名称',
    `stock_type` TINYINT DEFAULT 0 COMMENT '证券类别(0:股票,1:基金)',
    `entrust_bs` TINYINT NOT NULL COMMENT '买卖方向(1:买入,2:卖出)',
    `entrust_amount` DECIMAL(18,4) NOT NULL COMMENT '委托数量',
    `entrust_price` DECIMAL(18,4) NOT NULL COMMENT '委托价格',
    `business_amount` DECIMAL(18,4) DEFAULT 0 COMMENT '成交数量',
    `withdraw_amount` DECIMAL(18,4) DEFAULT 0 COMMENT '撤单数量',
    `business_price` DECIMAL(18,4) DEFAULT 0 COMMENT '成交价格',
    `entrust_status` TINYINT DEFAULT 0 COMMENT '委托状态(0:未报,1:已报,2:部分成交,3:已成,4:部撤,5:已撤,6:废单,7:待报)',
    `entrust_prop` VARCHAR(10) DEFAULT '普通' COMMENT '委托属性',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_client_id` (`client_id`),
    INDEX `idx_fund_account` (`fund_account`),
    INDEX `idx_stock_account` (`stock_account`),
    INDEX `idx_stock_code` (`stock_code`),
    INDEX `idx_entrust_status` (`entrust_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='委托表';