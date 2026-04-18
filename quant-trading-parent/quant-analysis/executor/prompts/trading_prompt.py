"""
交易决策Prompt模板
"""

TRADING_DECISION_PROMPT = """你是一名专业的交易员。请综合以下多维度分析报告，制定最终的交易计划：

股票代码: {stock_code}
股票名称: {stock_name}
当前价格: {current_price}元

=== 基本面分析 ===
{fundamentals_report}

=== 技术面分析 ===
{market_report}

=== 新闻分析 ===
{news_report}

=== 舆情分析 ===
{sentiment_report}

=== 看涨观点 ===
{bull_argument}

=== 看跌观点 ===
{bear_argument}

请制定详细的交易计划，包含：

1. **最终决策**：BUY / SELL / HOLD
2. **置信度**：0-100%
3. **目标价**：具体价格和预期涨幅
4. **止损价**：具体价格和止损幅度
5. **仓位建议**：轻仓/半仓/重仓
6. **入场时机**：立即/等待回调/分批建仓
7. **持有期限**：短线/中线/长线
8. **核心逻辑**：支撑决策的关键理由

请给出专业、客观的交易建议。"""

TRADING_PLAN_TEMPLATE = """
## 交易计划

**决策**: {decision}
**置信度**: {confidence}%
**目标价**: {target_price}元 (预期涨幅: {expected_return}%)
**止损价**: {stop_loss}元 (止损幅度: {stop_loss_percent}%)

### 仓位管理
- 建议仓位: {position_ratio}
- 入场时机: {entry_timing}
- 持有期限: {holding_period}

### 核心逻辑
{core_logic}
"""