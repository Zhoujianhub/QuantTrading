"""
看跌研究员 Agent
基于基本面、技术面、新闻、舆情分析，生成看跌观点
"""
from typing import Dict, Any
import logging

logger = logging.getLogger(__name__)


class BearResearcher:
    """看跌研究员 - 从负面角度分析股票"""
    
    def __init__(self, llm):
        self.llm = llm
    
    def research(self, state: Dict[str, Any]) -> Dict[str, Any]:
        """
        生成看跌观点
        
        Args:
            state: AgentState，包含各维度分析报告
        
        Returns:
            更新后的state，包含bear_argument
        """
        stock_code = state.get('stock_code', '')
        stock_name = state.get('stock_name', stock_code)
        
        logger.info(f"[看跌研究员] 研究 {stock_code}")
        
        try:
            # 构建研究提示
            prompt = self._build_research_prompt(state)
            
            # 调用LLM进行推理
            response = self.llm.invoke(prompt)
            bear_argument = str(response) if response else "看跌研究无结果"
            
            return {
                'bear_argument': bear_argument,
                'bear_score': self._calculate_bear_score(state)
            }
            
        except Exception as e:
            logger.error(f"[看跌研究员] 研究失败: {e}")
            return {
                'bear_argument': f"看跌研究失败: {str(e)}",
                'bear_score': 0
            }
    
    def _build_research_prompt(self, state: Dict[str, Any]) -> str:
        """构建研究提示"""
        fundamentals = state.get('fundamentals_report', '暂无基本面数据')
        market = state.get('market_report', '暂无技术面数据')
        news = state.get('news_report', '暂无新闻数据')
        sentiment = state.get('sentiment_report', '暂无舆情数据')
        
        return f"""你是一名专业的研究员。请基于以下多维度分析报告，从看跌角度深入研究这只股票：

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

请从以下维度生成审慎的看跌观点：

1. **估值过高**：PE/PB处于历史高位、透支未来增长
2. **业绩风险**：业绩增速放缓、盈利能力下滑、毛利率下降
3. **竞争恶化**：行业竞争加剧、市场份额流失
4. **技术破位**：关键技术支撑跌破、下降趋势形成
5. **风险事件**：解禁、减持、监管风险
6. **行业周期**：行业景气下行、赛道拥挤
7. **资金撤离**：机构减仓、流动性收缩

请给出具体的数据支撑和逻辑链条，形成审慎的看跌论点。"""
    
    def _calculate_bear_score(self, state: Dict[str, Any]) -> int:
        """计算看跌评分"""
        score = 50
        if state.get('fundamentals_score', 0) < 40:
            score += 10
        if state.get('market_score', 0) < 40:
            score += 10
        if state.get('news_score', 0) < 40:
            score += 10
        if state.get('sentiment_score', 0) < 40:
            score += 10
        return min(score, 100)