# 架构文档

## 系统架构

### 整体架构

```mermaid
graph TB
    subgraph Frontend["前端层"]
        WEB[Web UI]
        MOBILE[Mobile App]
    end

    subgraph Gateway["网关层"]
        GATEWAY[API Gateway<br/>Spring Cloud Gateway]
    end

    subgraph Services["业务服务层"]
        ACCT[quant-account<br/>账户服务]
        POS[quant-position<br/>持仓服务]
        TRADE[quant-trade<br/>交易服务]
        MKT[quant-market<br/>市场服务]
        MON[quant-monitor<br/>监控服务]
        NOTIF[quant-notification<br/>通知服务]
        ANAL[quant-analysis<br/>分析服务]
    end

    subgraph AI["AI 引擎层"]
        EXECUTOR[Agent Executor<br/>Python]
        AGENTS[多Agent系统]
        PROMPT[Prompt模板]
        TOOLS[Market Data Tool<br/>Sentiment Tool]
    end

    subgraph Data["数据层"]
        MYSQL[(MySQL<br/>主数据库)]
        REDIS[(Redis<br/>缓存)]
        EXTERNAL[外部API<br/>新浪/东方财富]
    end

    WEB --> GATEWAY
    MOBILE --> GATEWAY
    GATEWAY --> ACCT
    GATEWAY --> POS
    GATEWAY --> TRADE
    GATEWAY --> MKT
    GATEWAY --> MON
    GATEWAY --> NOTIF
    GATEWAY --> ANAL
    ANAL <--> EXECUTOR
    EXECUTOR <--> AGENTS
    EXECUTOR <--> PROMPT
    EXECUTOR <--> TOOLS
    TOOLS --> EXTERNAL
    POS --> MYSQL
    MON --> MYSQL
    NOTIF --> MYSQL
    ANAL --> REDIS
    POS --> REDIS
```

## 模块设计

### quant-common

公共组件模块，提供系统级基础设施：

```
quant-common/
├── exception/          # 全局异常处理
├── result/             # 统一响应格式
├── constants/          # 常量定义
└── util/               # 工具类
```

**核心类**:
- `Result<T>` - 统一API响应包装
- `ResultCode` - 响应码枚举
- `GlobalExceptionHandler` - 全局异常处理器
- `BusinessException` - 业务异常

### quant-position

持仓管理模块，核心业务模块：

```
quant-position/
├── entity/             # 持仓实体
├── repository/         # 数据访问层
├── service/            # 业务服务
│   ├── fund/           # 基金净值服务
│   └── quote/          # 行情服务
├── client/             # 外部API客户端
│   ├── sina/           # 新浪行情
│   └── eastmoney/      # 东方财富
├── scheduler/          # 定时任务
├── dto/                # 数据传输对象
└── controller/         # REST控制器
```

**数据库表**:
- `holding` - 持仓表
- `fund_nav_history` - 基金净值历史
- `etf_price_history` - ETF价格历史

### quant-monitor

监控规则与事件处理：

```
quant-monitor/
├── entity/             # 监控规则/事件实体
├── repository/          # 数据访问层
├── service/            # 业务服务
├── scheduler/          # 定时评估调度器
├── client/             # 通知客户端
└── controller/         # REST控制器
```

### quant-notification

通知服务，支持多渠道：

```
quant-notification/
├── entity/             # 通知渠道/记录实体
├── repository/         # 数据访问层
├── service/            # 业务服务
├── sender/             # 发送器实现
│   ├── NotificationSender  # 发送接口
│   └── ConsoleSender       # 控制台发送器
└── controller/         # REST控制器
```

### quant-analysis

分析服务模块，桥接Java与Python AI引擎：

```
quant-analysis/
├── controller/         # REST控制器
└── executor/           # Python AI执行器
    ├── agent_executor.py   # Agent执行器主类
    ├── agents/             # 各类Agent实现
    ├── prompts/             # Prompt模板
    ├── tools/               # 工具集
    └── server.py            # Flask服务入口
```

## AI Agent 系统架构

```mermaid
graph LR
    subgraph Input["输入"]
        STOCK[股票代码]
    end

    subgraph Stage1["Stage 1 并行分析"]
        FA[基本面分析]
        MA[市场分析]
        NA[新闻分析]
        SA[舆情分析]
    end

    subgraph Stage2["Stage 2 多空辩论"]
        BULL[看涨研究员]
        BEAR[看跌研究员]
    end

    subgraph Stage3["Stage 3 决策"]
        TRADER[交易员]
        RISK[风险辩论]
    end

    subgraph Output["输出"]
        REPORT[分析报告]
    end

    STOCK --> Stage1
    Stage1 --> Stage2
    Stage2 --> Stage3
    Stage3 --> REPORT
```

### Agent 类型

| Agent | 职责 | 输出 |
|-------|------|------|
| FundamentalsAnalyst | 基本面分析 | fundamentals_report, fundamentals_score |
| MarketAnalyst | 市场走势分析 | market_report, market_score |
| NewsAnalyst | 新闻事件分析 | news_report, news_score |
| SentimentAnalyst | 舆情分析 | sentiment_report, sentiment_score |
| BullResearcher | 看涨观点研究 | bull_argument |
| BearResearcher | 看跌观点研究 | bear_argument |
| Trader | 交易决策 | decision, investment_plan, confidence |
| RiskDebator | 风险评估 | risk_analysis, risk_level |

## 数据流

```mermaid
sequenceDiagram
    participant Client
    participant API
    participant Position
    participant Monitor
    participant Analysis
    participant AI
    participant Redis

    Client->>API: 请求持仓分析
    API->>Position: 获取持仓数据
    Position->>Redis: 缓存行情
    Position-->>API: 持仓信息
    API->>Analysis: 触发AI分析
    Analysis->>AI: 调用Agent Executor
    AI->>AI: 并行执行多Agent
    AI-->>Analysis: 分析报告
    Analysis-->>API: 整合结果
    API-->>Client: 返回分析报告

    Note over AI: 实时进度回调
    AI->>API: 进度更新
    API->>Client: SSE推送进度
```

## 技术选型

| 类别 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 17 |
| 框架 | Spring Boot | 3.1.8 |
| ORM | MyBatis-Plus | 3.5.5 |
| 数据库 | MySQL | 8.3.0 |
| 缓存 | Redis | 6.0+ |
| 任务调度 | XXL-Job | 2.4.1 |
| API文档 | SpringDoc OpenAPI | 2.5.0 |
| AI框架 | LangGraph | 0.0.20+ |
| LLM | Claude/DeepSeek | - |
| Python | Flask | 3.0.0 |
