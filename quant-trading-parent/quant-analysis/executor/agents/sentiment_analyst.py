"""
舆情分析师 Agent
负责分析社交媒体和论坛上的投资者情绪
"""
from typing import Dict, Any
import logging

logger = logging.getLogger(__name__)


class SentimentAnalyst:
    """舆情分析师 - 分析社交媒体情绪、投资者关注度"""
    
    def __init__(self, llm):
        self.llm = llm
    
    def analyze(self, state: Dict[str, Any]) -> Dict[str, Any]:
        """
        执行舆情分析
        
        Args:
            state: AgentState，包含stock_code
        
        Returns:
            更新后的state，包含sentiment_report
        """
        stock_code = state.get('stock_code', '')
        stock_name = state.get('stock_name', stock_code)
        
        logger.info(f"[舆情分析师] 分析 {stock_code}")
        
        try:
            # 获取舆情数据
            sentiment_data = self._get_sentiment_data(stock_code)
            
            # 构建分析提示
            prompt = self._build_analysis_prompt(stock_code, stock_name, sentiment_data)
            
            # 调用LLM进行分析
            response = self.llm.invoke(prompt)
            analysis = str(response) if response else "舆情分析无结果"
            
            return {
                'sentiment_report': analysis,
                'sentiment_data': sentiment_data,
                'sentiment_score': sentiment_data.get('overall_sentiment', 0)
            }
            
        except Exception as e:
            logger.error(f"[舆情分析师] 分析失败: {e}")
            return {
                'sentiment_report': f"舆情分析失败: {str(e)}",
                'sentiment_score': 0
            }
    
    def _get_sentiment_data(self, stock_code: str) -> Dict[str, Any]:
        """
        获取舆情数据
        实际项目中应该调用真实的舆情API（如东方财富股吧、雪球等）
        """
        # 模拟舆情数据
        return {
            'platform_sentiment': {
                'eastmoney': 0.68,  # 东方财富
                'xueqiu': 0.72,      # 雪球
                'tonghuashun': 0.55  # 同花顺
            },
            'overall_sentiment': 0.65,  # 0-1，0.5为中性
            'investor_attention': 8500,  # 投资者关注度指数
            'sentiment_trend': 'rising',  # trending, falling, stable
            'hot_posts': [
                {
                    'platform': '东方财富股吧',
                    'title': '业绩超预期，股价要起飞？',
                    'sentiment': 'positive',
                    'replies': 256,
                    'likes': 1024
                },
                {
                    'platform': '雪球',
                    'title': '估值合理，回调就是机会',
                    'sentiment': 'positive',
                    'replies': 89,
                    'likes': 456
                },
                {
                    'platform': '同花顺',
                    'title': '技术面有压力，谨慎追高',
                    'sentiment': 'neutral',
                    'replies': 45,
                    'likes': 123
                }
            ],
            'key_discussions': [
                '业绩增长超预期',
                '回购计划利好',
                '行业龙头地位稳固',
                '估值是否偏高'
            ],
            'bull_bear_ratio': 1.8,  # 多空比 > 1 看多
            'fear_greed_index': 62,  # 0-100, >50看多
        }
    
    def _build_analysis_prompt(self, stock_code: str, stock_name: str,
                                sentiment_data: Dict[str, Any]) -> str:
        """构建分析提示"""
        platform_sentiment = sentiment_data.get('platform_sentiment', {})
        hot_posts = sentiment_data.get('hot_posts', [])
        key_discussions = sentiment_data.get('key_discussions', [])
        
        posts_list = "\n".join([
            f"- [{p['platform']}] {p['title']} (情感:{p['sentiment']}, 回复:{p['replies']}, 点赞:{p['likes']})"
            for p in hot_posts
        ])
        
        return f"""你是一名专业的舆情分析师。请分析以下股票的投资者情绪和舆情情况：

股票代码: {stock_code}
股票名称: {stock_name}

各平台情感评分:
- 东方财富: {platform_sentiment.get('eastmoney', 0) * 100:.0f}%
- 雪球: {platform_sentiment.get('xueqiu', 0) * 100:.0f}%
- 同花顺: {platform_sentiment.get('tonghuashun', 0) * 100:.0f}%

整体情感评分: {sentiment_data.get('overall_sentiment', 0) * 100:.0f}%
投资者关注度: {sentiment_data.get('investor_attention', 0)} (越高越热门)
情感趋势: {sentiment_data.get('sentiment_trend', 'N/A')}
多空比: {sentiment_data.get('bull_bear_ratio', 1.0):.2f} (>1看多)
恐惧贪婪指数: {sentiment_data.get('fear_greed_index', 50)} (0-100, >50看多)

热门帖子:
{posts_list}

热点讨论话题:
{', '.join(key_discussions) if key_discussions else '暂无'}

请从以下几个维度进行分析：
1. 各平台情感对比分析
2. 投资者关注度变化趋势
3. 多空双方观点博弈
4. 热门讨论反映的市场预期
5. 舆情面对股价的影响判断

请用专业的分析语言，给出详细的舆情分析报告。"""