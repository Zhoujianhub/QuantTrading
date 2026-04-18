"""
交易员 Agent
综合多Agent分析结果，制定最终交易计划
"""
from typing import Dict, Any
import logging

logger = logging.getLogger(__name__)


class Trader:
    """交易员 - 综合分析制定交易计划"""
    
    def __init__(self, llm):
        self.llm = llm
    
    def decide(self, state: Dict[str, Any]) -> Dict[str, Any]:
        """
        制定交易决策
        
        Args:
            state: AgentState，包含各维度分析和辩论结果
        
        Returns:
            更新后的state，包含decision和investment_plan
        """
        stock_code = state.get('stock_code', '')
        stock_name = state.get('stock_name', stock_code)
        
        logger.info(f"[交易员] 决策 {stock_code}")
        
        try:
            # 综合所有分析结果
            bull = state.get('bull_argument', '暂无看涨观点')
            bear = state.get('bear_argument', '暂无看跌观点')
            
            # 构建决策提示
            prompt = self._build_decision_prompt(state, bull, bear)
            
            # 调用LLM进行决策
            response = self.llm.invoke(prompt)
            decision_result = str(response) if response else "交易计划无结果"
            
            # 解析决策结果
            decision = self._parse_decision(decision_result)
            
            return {
                'investment_plan': decision_result,
                'decision': decision.get('action', 'HOLD'),
                'confidence': decision.get('confidence', 0.5),
                'target_price': decision.get('target_price', 0),
                'stop_loss': decision.get('stop_loss', 0),
                'position_ratio': decision.get('position_ratio', 0)
            }
            
        except Exception as e:
            logger.error(f"[交易员] 决策失败: {e}")
            return {
                'investment_plan': f"交易决策失败: {str(e)}",
                'decision': 'HOLD',
                'confidence': 0
            }
    
    def _parse_decision(self, decision_result: str) -> Dict[str, Any]:
        """解析交易决策结果"""
        result = {'action': 'HOLD', 'confidence': 0.5, 'target_price': 0, 'stop_loss': 0, 'position_ratio': 0}
        
        decision_result_upper = decision_result.upper()
        
        if 'BUY' in decision_result_upper:
            result['action'] = 'BUY'
        elif 'SELL' in decision_result_upper:
            result['action'] = 'SELL'
        
        import re
        confidence_match = re.search(r'置信[度D]*[:：]?\s*(\d+)%?', decision_result)
        if confidence_match:
            result['confidence'] = int(confidence_match.group(1)) / 100
        
        return result
    
    def _build_decision_prompt(self, state: Dict[str, Any], bull: str, bear: str) -> str:
        """构建决策提示"""
        fundamentals = state.get('fundamentals_report', '暂无')
        market = state.get('market_report', '暂无')
        news = state.get('news_report', '暂无')
        sentiment = state.get('sentiment_report', '暂无')
        
        return f"""你是一名专业的交易员。请综合以下多维度分析报告，制定最终的交易计划：

股票代码: {state.get('stock_code')}
股票名称: {state.get('stock_name', state.get('stock_code'))}
当前价格: {state.get('current_price', 'N/A')}元

=== 基本面分析 ===
{fundamentals[:500]}...

=== 技术面分析 ===
{market[:500]}...

=== 新闻分析 ===
{news[:500]}...

=== 舆情分析 ===
{sentiment[:500]}...

=== 看涨观点 ===
{bull[:800]}...

=== 看跌观点 ===
{bear[:800]}...

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