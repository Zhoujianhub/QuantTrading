"""
技术分析Prompt模板
"""

MARKET_ANALYSIS_PROMPT = """你是一名专业的技术分析师。请分析以下股票的技术面情况：

股票代码: {stock_code}
股票名称: {stock_name}

当前行情:
- 当前价格: {current_price}元
- 涨跌幅: {change_percent}%
- 成交量: {volume}百万手
- 换手率: {turnover_rate}%
- 52周最高: {high_52w}元
- 52周最低: {low_52w}元

均线系统:
- MA5: {ma5}元
- MA10: {ma10}元
- MA20: {ma20}元
- MA60: {ma60}元

技术指标:
- RSI(14): {rsi}
- MACD: DIFF={macd_diff}, DEA={macd_dea}, HIST={macd_hist}
- KDJ: K={kdj_k}, D={kdj_d}, J={kdj_j}
- BOLL: 上轨={boll_upper}, 中轨={boll_middle}, 下轨={boll_lower}

趋势判断: {trend}
支撑位: {support_level}元
阻力位: {resistance_level}元

请从以下几个维度进行分析：
1. 趋势判断（当前趋势、均线排列）
2. 动能分析（RSI、MACD）
3. 趋势强度（KDJ、BOLL）
4. 关键价位分析（支撑、阻力）
5. 综合技术面评级

请用专业的技术分析语言，给出详细的技术分析报告。"""

TECHNICAL_SCORE_PROMPT = """请评估这只股票的技术面评分（0-100分）：

{analysis_report}

请给出：
1. 技术面总分
2. 各项指标得分（趋势、动能、波动性）
3. 关键信号提示（买入/卖出/持有）"""