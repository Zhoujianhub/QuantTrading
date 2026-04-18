"""
新闻分析师 Agent
负责分析与股票相关的新闻事件及其影响
"""
from typing import Dict, Any
import logging

logger = logging.getLogger(__name__)


class NewsAnalyst:
    """新闻分析师 - 分析新闻事件、行业动态、政策影响"""
    
    def __init__(self, llm):
        self.llm = llm
    
    def analyze(self, state: Dict[str, Any]) -> Dict[str, Any]:
        """
        执行新闻分析
        
        Args:
            state: AgentState，包含stock_code
        
        Returns:
            更新后的state，包含news_report
        """
        stock_code = state.get('stock_code', '')
        stock_name = state.get('stock_name', stock_code)
        
        logger.info(f"[新闻分析师] 分析 {stock_code}")
        
        try:
            # 获取新闻数据
            news_data = self._get_news_data(stock_code)
            
            # 构建分析提示
            prompt = self._build_analysis_prompt(stock_code, stock_name, news_data)
            
            # 调用LLM进行分析
            response = self.llm.invoke(prompt)
            analysis = str(response) if response else "新闻分析无结果"
            
            return {
                'news_report': analysis,
                'news_data': news_data,
                'news_sentiment_score': news_data.get('overall_sentiment', 0)
            }
            
        except Exception as e:
            logger.error(f"[新闻分析师] 分析失败: {e}")
            return {
                'news_report': f"新闻分析失败: {str(e)}",
                'news_score': 0
            }
    
    def _get_news_data(self, stock_code: str) -> Dict[str, Any]:
        """
        获取新闻数据
        实际项目中应该调用真实的新闻API
        """
        # 模拟新闻数据
        return {
            'recent_news': [
                {
                    'title': '公司发布年度业绩预告，净利润增长20%',
                    'date': '2026-04-08',
                    'sentiment': 'positive',
                    'source': '财经网',
                    'impact': 'high'
                },
                {
                    'title': '行业政策利好，新能源汽车补贴延续',
                    'date': '2026-04-05',
                    'sentiment': 'positive',
                    'source': '发改委官网',
                    'impact': 'medium'
                },
                {
                    'title': '竞争对手发布新品，市场竞争加剧',
                    'date': '2026-04-03',
                    'sentiment': 'negative',
                    'source': '行业媒体',
                    'impact': 'medium'
                },
                {
                    'title': '公司宣布回购股份计划',
                    'date': '2026-04-01',
                    'sentiment': 'positive',
                    'source': '公司公告',
                    'impact': 'high'
                }
            ],
            'industry_news': [
                {
                    'title': '行业整体保持稳定增长',
                    'date': '2026-04-06',
                    'sentiment': 'positive',
                    'source': '行业协会'
                }
            ],
            'policy_news': [
                {
                    'title': '碳中和政策持续推进，行业龙头受益',
                    'date': '2026-04-07',
                    'sentiment': 'positive',
                    'source': '政策研究'
                }
            ],
            'overall_sentiment': 0.65,  # 0-1，0.5为中性
            'news_count': 8,
            'key_events': [
                '年度业绩预增20%',
                '股份回购计划',
                '行业政策利好'
            ]
        }
    
    def _build_analysis_prompt(self, stock_code: str, stock_name: str,
                               news_data: Dict[str, Any]) -> str:
        """构建分析提示"""
        recent_news = news_data.get('recent_news', [])
        industry_news = news_data.get('industry_news', [])
        policy_news = news_data.get('policy_news', [])
        key_events = news_data.get('key_events', [])
        
        news_list = "\n".join([
            f"- [{n['date']}] {n['title']} (情感:{n['sentiment']}, 影响:{n['impact']})"
            for n in recent_news[:5]
        ])
        
        industry_list = "\n".join([
            f"- [{n['date']}] {n['title']}"
            for n in industry_news
        ])
        
        policy_list = "\n".join([
            f"- [{n['date']}] {n['title']}"
            for n in policy_news
        ])
        
        return f"""你是一名专业的新闻分析师。请分析以下股票的新闻舆情情况：

股票代码: {stock_code}
股票名称: {stock_name}

近期重要新闻:
{news_list}

行业动态:
{industry_list if industry_list else '暂无'}

政策相关:
{policy_list if policy_list else '暂无'}

关键事件:
{', '.join(key_events) if key_events else '暂无'}

整体新闻情感评分: {news_data.get('overall_sentiment', 0) * 100:.0f}% (0-100%)

请从以下几个维度进行分析：
1. 新闻情感分析（整体是正面还是负面）
2. 重要事件影响评估
3. 行业环境分析
4. 政策面对股价的影响
5. 新闻面综合评级

请用专业的分析语言，给出详细的新闻分析报告，特别关注对公司股价的潜在影响。"""