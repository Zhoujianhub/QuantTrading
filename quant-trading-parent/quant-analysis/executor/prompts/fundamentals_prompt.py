"""
基本面分析Prompt模板
"""

FUNDAMENTALS_ANALYSIS_PROMPT = """你是一名专业的基本面分析师。请分析以下股票的基本面情况：

股票代码: {stock_code}
股票名称: {stock_name}

财务数据:
- 市盈率(PE): {pe_ratio}
- 市净率(PB): {pb_ratio}
- 净资产收益率(ROE): {roe}%
- 营收增长率: {revenue_growth}%
- 利润增长率: {profit_growth}%
- 资产负债率: {debt_ratio}%
- 流动比率: {current_ratio}
- 股息收益率: {dividend_yield}%
- 总市值: {market_cap}亿元

请从以下几个维度进行分析：
1. 估值水平分析（PE、PB是否合理）
2. 盈利能力分析（ROE、利润增长率）
3. 财务健康度（资产负债率、流动比率）
4. 成长性评估
5. 投资价值总结

请用专业的分析语言，给出详细的基本面分析报告。"""

FUNDAMENTALS_SCORE_PROMPT = """请评估这只股票的基本面评分（0-100分）：

{analysis_report}

请给出：
1. 基本面总分
2. 各项维度得分（估值、盈利、财务、成长）
3. 优劣势总结"""