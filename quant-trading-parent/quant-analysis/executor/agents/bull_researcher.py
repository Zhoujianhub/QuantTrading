"""
看涨研究员 Agent
基于基本面、技术面、新闻、舆情分析，生成看涨观点
"""
from typing import Dict, Any
import logging

logger = logging.getLogger(__name__)


class BullResearcher:
    """看涨研究员 - 从正面角度分析股票"""
    
    def __init__(self, llm):
        self.llm = llm
    
    def research(self, state: Dict[str, Any]) -> Dict[str, Any]:
        """
        生成看涨观点
        
        Args:
            state: AgentState，包含各维度分析报告
        
        Returns:
            更新后的state，包含bull_argument
        """
        stock_code = state.get('stock_code', '')
        stock_name = state.get('stock_name', stock_code)
        
        logger.info(f"[看涨研究员] 研究 {stock_code}")
        
        try:
            # 构建研究提示
            prompt = self._build_research_prompt(state)
            
            # 调用LLM进行推理
            response = self.llm.invoke(prompt)
            bull_argument = str(response) if response else "看涨研究无结果"
            
            return {
                'bull_argument': bull_argument,
                'bull_score': self._calculate_bull_score(state)
            }
            
        except Exception as e:
            logger.error(f"[看涨研究员] 研究失败: {e}")
            return {
                'bull_argument': f"看涨研究失败: {str(e)}",
                'bull_score': 0
            }
    
    def _calculate_bull_score(self, state: Dict[str, Any]) -> int:
        """计算看涨评分"""
        score = 50
        if state.get('fundamentals_score', 0) > 70:
            score += 10
        if state.get('market_score', 0) > 70:
            score += 10
        if state.get('news_score', 0) > 70:
            score += 10
        if state.get('sentiment_score', 0) > 70:
            score += 10
        return min(score, 100)
    
    def _build_research_prompt(self, state: Dict[str, Any]) -> str:
        """构建研究提示"""
        fundamentals = state.get('fundamentals_report', '暂无基本面数据')
        market = state.get('market_report', '暂无技术面数据')
        news = state.get('news_report', '暂无新闻数据')
        sentiment = state.get('sentiment_report', '暂无舆情数据')
        
        return f"""你是一名专业的研究员。请基于以下多维度分析报告，从看涨角度深入研究这只股票：

股票代码: {state.get('stock_code')}
股票名称: {state.get('stock_name', state.get('stock_code'))}

=== 基本面分析 ===
{fundamentals}

=== 技术面分析 ===
{market}

=== 新闻分析 ===
{news}

=== 舆情分析 ===
{sentiment}

请从以下维度生成有力的看涨观点：

1. **价值发现**：估值洼地、价值重估机会
2. **成长逻辑**：业绩增长驱动力、市场扩张空间
3. **催化剂**：即将发生的利好事件（政策、业绩、产品发布等）
4. **技术突破**：关键技术位突破、趋势转强信号
5. **资金面**：机构增持、流动性改善
6. **行业景气**：行业周期向上、赛道红利

请给出具体的数据支撑和逻辑链条，形成有力的看涨论点。"""