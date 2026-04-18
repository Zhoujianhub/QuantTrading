"""
新闻分析Prompt模板
"""

NEWS_ANALYSIS_PROMPT = """你是一名专业的新闻分析师。请分析以下股票的新闻媒体情况：

股票代码: {stock_code}
股票名称: {stock_name}

近期重要新闻:
{recent_news}

行业动态:
{industry_news}

政策相关:
{policy_news}

关键事件:
{key_events}

整体新闻情感评分: {overall_sentiment}%

请从以下几个维度进行分析：
1. 新闻情感分析（整体是正面还是负面）
2. 重要事件影响评估
3. 行业环境分析
4. 政策面对股价的影响
5. 新闻面综合评级

请用专业的分析语言，给出详细的新闻分析报告，特别关注对公司股价的潜在影响。"""

SENTIMENT_ANALYSIS_PROMPT = """你是一名专业的舆情分析师。请分析以下股票的投资者情绪情况：

股票代码: {stock_code}
股票名称: {stock_name}

各平台情感评分:
{platform_sentiment}

热门帖子:
{hot_posts}

热点讨论话题:
{key_discussions}

多空比: {bull_bear_ratio}
恐惧贪婪指数: {fear_greed_index}

请分析：
1. 各平台情感对比
2. 投资者关注度变化
3. 多空双方观点博弈
4. 市场预期判断"""