-- ================================================
-- Multi-Agent Analysis Module Database Schema
-- 股票多Agent分析模块数据库表结构
-- ================================================

-- 分析请求表
CREATE TABLE IF NOT EXISTS `analysis_request` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `request_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '请求唯一标识',
    `stock_code` VARCHAR(20) NOT NULL COMMENT '股票代码',
    `stock_name` VARCHAR(100) COMMENT '股票名称',
    `account_id` VARCHAR(64) COMMENT '账户ID',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/RUNNING/COMPLETED/FAILED/CANCELLED',
    `progress` INT DEFAULT 0 COMMENT '进度 0-100',
    `current_stage` VARCHAR(50) COMMENT '当前阶段',
    `analysis_dimensions` VARCHAR(255) COMMENT '分析维度,逗号分隔: FUNDAMENTALS,MARKET,NEWS,SENTIMENT',
    `error_message` TEXT COMMENT '错误信息',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `completed_at` DATETIME COMMENT '完成时间',
    `request_params` JSON COMMENT '请求参数JSON',
    INDEX `idx_stock_code` (`stock_code`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股票分析请求表';

-- 分析报告表
CREATE TABLE IF NOT EXISTS `analysis_report` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `report_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '报告唯一标识',
    `request_id` VARCHAR(64) NOT NULL COMMENT '关联请求ID',
    `stock_code` VARCHAR(20) NOT NULL COMMENT '股票代码',
    `stock_name` VARCHAR(100) COMMENT '股票名称',
    
    -- 决策结果
    `decision` VARCHAR(20) NOT NULL COMMENT '决策: BUY/SELL/HOLD',
    `confidence` DECIMAL(5,2) DEFAULT 0 COMMENT '置信度 0-100',
    `risk_level` VARCHAR(20) DEFAULT 'MEDIUM' COMMENT '风险等级: LOW/MEDIUM/HIGH',
    
    -- 各维度分析报告
    `fundamentals_report` TEXT COMMENT '基本面分析报告',
    `market_report` TEXT COMMENT '技术面分析报告',
    `news_report` TEXT COMMENT '新闻分析报告',
    `sentiment_report` TEXT COMMENT '舆情分析报告',
    
    -- 辩论观点
    `bull_argument` TEXT COMMENT '看涨观点',
    `bear_argument` TEXT COMMENT '看跌观点',
    
    -- 交易计划
    `investment_plan` TEXT COMMENT '投资计划',
    `target_price` DECIMAL(10,2) COMMENT '目标价',
    `stop_loss` DECIMAL(10,2) COMMENT '止损价',
    `position_ratio` VARCHAR(20) COMMENT '仓位建议',
    
    -- 风险评估
    `risk_analysis` TEXT COMMENT '风险评估',
    `risk_score` INT DEFAULT 50 COMMENT '风险评分 0-100',
    
    -- 最终报告
    `final_report` TEXT COMMENT '最终Markdown报告',
    
    -- 评分
    `fundamentals_score` INT COMMENT '基本面评分',
    `market_score` INT COMMENT '技术面评分',
    `news_score` INT COMMENT '新闻评分',
    `sentiment_score` INT COMMENT '舆情评分',
    
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX `idx_request_id` (`request_id`),
    INDEX `idx_stock_code` (`stock_code`),
    INDEX `idx_decision` (`decision`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股票分析报告表';

-- Agent执行日志表
CREATE TABLE IF NOT EXISTS `analysis_agent_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `log_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '日志唯一标识',
    `request_id` VARCHAR(64) NOT NULL COMMENT '关联请求ID',
    `agent_type` VARCHAR(50) NOT NULL COMMENT 'Agent类型',
    `agent_name` VARCHAR(100) COMMENT 'Agent名称',
    `stage` VARCHAR(50) COMMENT '执行阶段',
    `status` VARCHAR(20) NOT NULL COMMENT '状态: STARTED/RUNNING/COMPLETED/FAILED',
    `input_data` JSON COMMENT '输入数据',
    `output_data` JSON COMMENT '输出数据',
    `error_message` TEXT COMMENT '错误信息',
    `execution_time_ms` BIGINT COMMENT '执行时间(毫秒)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX `idx_request_id` (`request_id`),
    INDEX `idx_agent_type` (`agent_type`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent执行日志表';

-- 批量分析批次表
CREATE TABLE IF NOT EXISTS `analysis_batch` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `batch_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '批次唯一标识',
    `account_id` VARCHAR(64) COMMENT '账户ID',
    `total_count` INT DEFAULT 0 COMMENT '总数量',
    `completed_count` INT DEFAULT 0 COMMENT '完成数量',
    `failed_count` INT DEFAULT 0 COMMENT '失败数量',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX `idx_batch_id` (`batch_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='批量分析批次表';

-- 批量分析明细表
CREATE TABLE IF NOT EXISTS `analysis_batch_item` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `item_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '明细唯一标识',
    `batch_id` VARCHAR(64) NOT NULL COMMENT '批次ID',
    `stock_code` VARCHAR(20) NOT NULL COMMENT '股票代码',
    `stock_name` VARCHAR(100) COMMENT '股票名称',
    `request_id` VARCHAR(64) COMMENT '关联请求ID',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    `error_message` TEXT COMMENT '错误信息',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX `idx_batch_id` (`batch_id`),
    INDEX `idx_stock_code` (`stock_code`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='批量分析明细表';