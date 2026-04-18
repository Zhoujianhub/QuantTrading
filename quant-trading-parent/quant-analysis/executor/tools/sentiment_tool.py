"""
舆情数据工具类
用于获取股票的舆情数据（社交媒体、新闻情绪等）
"""
from typing import Dict, Any, List, Optional
import logging

logger = logging.getLogger(__name__)


class SentimentTool:
    """舆情数据获取工具"""
    
    def __init__(self, config: Optional[Dict[str, Any]] = None):
        self.config = config or {}
        self.data_source = self.config.get('data_source', 'mock')
    
    def get_platform_sentiment(self, stock_code: str) -> Dict[str, float]:
        """
        获取各平台情感评分
        
        Returns:
            各平台情感评分字典，0-1之间，0.5为中性
        """
        logger.info(f"[SentimentTool] 获取平台情感: {stock_code}")
        
        if self.data_source == 'mock':
            return self._mock_platform_sentiment()
        else:
            # TODO: 实现真实数据源调用
            return self._mock_platform_sentiment()
    
    def get_sentiment_trend(self, stock_code: str, days: int = 7) -> List[Dict[str, Any]]:
        """
        获取情感趋势
        
        Args:
            stock_code: 股票代码
            days: 趋势天数
        
        Returns:
            情感趋势列表
        """
        logger.info(f"[SentimentTool] 获取情感趋势: {stock_code}, days={days}")
        return self._mock_sentiment_trend(days)
    
    def get_hot_posts(self, stock_code: str, limit: int = 10) -> List[Dict[str, Any]]:
        """
        获取热门帖子
        
        Args:
            stock_code: 股票代码
            limit: 返回数量
        
        Returns:
            热门帖子列表
        """
        logger.info(f"[SentimentTool] 获取热门帖子: {stock_code}")
        return self._mock_hot_posts(limit)
    
    def get_investor_attention(self, stock_code: str) -> Dict[str, Any]:
        """
        获取投资者关注度
        
        Returns:
            关注度数据
        """
        logger.info(f"[SentimentTool] 获取投资者关注度: {stock_code}")
        return self._mock_investor_attention()
    
    def get_bull_bear_ratio(self, stock_code: str) -> float:
        """
        获取多空比
        
        Returns:
            多空比，>1看多，<1看空
        """
        logger.info(f"[SentimentTool] 获取多空比: {stock_code}")
        return 1.8
    
    def get_fear_greed_index(self, stock_code: str) -> int:
        """
        获取恐惧贪婪指数
        
        Returns:
            0-100，>50看多
        """
        logger.info(f"[SentimentTool] 获取恐惧贪婪指数: {stock_code}")
        return 62
    
    def _mock_platform_sentiment(self) -> Dict[str, float]:
        """模拟平台情感数据"""
        return {
            'eastmoney': 0.68,   # 东方财富
            'xueqiu': 0.72,      # 雪球
            'tonghuashun': 0.55, # 同花顺
            'weibo': 0.45,        # 微博
            'guba': 0.62          # 股吧
        }
    
    def _mock_sentiment_trend(self, days: int) -> List[Dict[str, Any]]:
        """模拟情感趋势"""
        import random
        from datetime import datetime, timedelta
        
        trend = []
        base_sentiment = 0.6
        
        for i in range(days):
            date = (datetime.now() - timedelta(days=days-i-1)).strftime('%Y-%m-%d')
            sentiment = max(0, min(1, base_sentiment + random.uniform(-0.1, 0.1)))
            trend.append({
                'date': date,
                'sentiment': round(sentiment, 3),
                'attention': int(random.uniform(5000, 10000))
            })
            base_sentiment = sentiment
        
        return trend
    
    def _mock_hot_posts(self, limit: int) -> List[Dict[str, Any]]:
        """模拟热门帖子"""
        posts = [
            {
                'platform': '东方财富股吧',
                'title': '业绩超预期，股价要起飞？',
                'author': '价值投资者',
                'sentiment': 'positive',
                'replies': 256,
                'likes': 1024,
                'views': 15000,
                'publish_time': '2小时前'
            },
            {
                'platform': '雪球',
                'title': '估值合理，回调就是机会',
                'author': '趋势交易者',
                'sentiment': 'positive',
                'replies': 89,
                'likes': 456,
                'views': 8000,
                'publish_time': '5小时前'
            },
            {
                'platform': '同花顺',
                'title': '技术面有压力，谨慎追高',
                'author': '技术派大师',
                'sentiment': 'neutral',
                'replies': 45,
                'likes': 123,
                'views': 5000,
                'publish_time': '8小时前'
            },
            {
                'platform': '微博',
                'title': '这个消息要小心了...',
                'author': '匿名用户',
                'sentiment': 'negative',
                'replies': 32,
                'likes': 67,
                'views': 3000,
                'publish_time': '1天前'
            }
        ]
        return posts[:limit]
    
    def _mock_investor_attention(self) -> Dict[str, Any]:
        """模拟投资者关注度"""
        return {
            'attention_index': 8500,  # 关注度指数
            'rank': 156,  # 全市场排名
            'trend': 'rising',  # rising, falling, stable
            'heat_level': 'hot',  # hot, warm, cold
            'search_volume': 125000  # 搜索量
        }