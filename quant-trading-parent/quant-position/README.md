# quant-position - 持仓管理模块

持仓管理模块，负责管理用户的基金/ETF持仓、净值计算与行情数据获取。

## 主要功能

- 持仓信息管理 (CRUD)
- 基金净值计算
- ETF 实时行情获取
- 持仓收益统计

## 技术栈

- Spring Boot 3.1.8
- MyBatis-Plus 3.5.5
- MySQL 8.0
- Redis 6.0
- XXL-Job (定时任务)

## 数据库表

### holding - 持仓表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| account_id | VARCHAR(64) | 账户ID |
| asset_code | VARCHAR(32) | 资产代码 |
| asset_name | VARCHAR(128) | 资产名称 |
| asset_type | VARCHAR(32) | 资产类型 (ETF/LOF/场外基金/股票) |
| trading_type | VARCHAR(16) | 交易类型 (场内/场外) |
| initial_fund | DECIMAL(16,4) | 初始投入金额 |
| current_position_amount | DECIMAL(16,4) | 当前持仓总金额 |
| cost_price | DECIMAL(12,4) | 持仓成本价 |
| current_nav | DECIMAL(12,4) | 当前净值/价格 |

### fund_nav_history - 基金净值历史

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| asset_code | VARCHAR(32) | 资产代码 |
| nav_date | DATE | 净值日期 |
| unit_nav | DECIMAL(12,4) | 单位净值 |
| accum_nav | DECIMAL(12,4) | 累计净值 |

### etf_price_history - ETF价格历史

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| asset_code | VARCHAR(32) | 资产代码 |
| price | DECIMAL(12,4) | 价格 |
| change_rate | DECIMAL(10,4) | 涨跌幅 |

## API 端点

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/holdings/{accountId} | 获取账户持仓列表 |
| POST | /api/holdings | 添加持仓 |
| PUT | /api/holdings/{id} | 更新持仓 |
| DELETE | /api/holdings/{id} | 删除持仓 |

## 外部 API

- **新浪行情**: `https://hq.sinajs.cn` - ETF 实时价格
- **东方财富**: `https://push2.eastmoney.com` - 基金净值数据

## 启动

```bash
mvn -pl quant-position spring-boot:run
```

服务端口: **8082**
