"""
风险辩论 Agent
从保守、中性、激进三个维度评估风险
"""
from typing import Dict, Any
import logging

logger = logging.getLogger(__name__)


class RiskDebator:
    """风险辩论者 - 多维度风险评估"""
    
    def __init__(self, llm):
        self.llm = llm
    
    def assess(self, state: Dict[str, Any]) -> Dict[str, Any]:
        """
        执行风险评估
        
        Args:
            state: AgentState，包含交易决策
        
        Returns:
            更新后的state，包含risk_analysis和risk_level
        """
        stock_code = state.get('stock_code', '')
        stock_name = state.get('stock_name', stock_code)
        
        logger.info(f"[风险辩论] 评估 {stock_code}")
        
        try:
            # 构建风险评估提示
            prompt = self._build_risk_prompt(state)
            
            # 调用LLM进行风险评估
            response = self.llm.invoke(prompt)
            risk_analysis = str(response) if response else "风险分析无结果"
            
            # 确定风险等级
            risk_level = self._determine_risk_level(state, risk_analysis)
            
            return {
                'risk_analysis': risk_analysis,
                'risk_level': risk_level,
                'risk_score': self._calculate_risk_score(risk_level)
            }
            
        except Exception as e:
            logger.error(f"[风险辩论] 评估失败: {e}")
            return {
                'risk_analysis': f"风险评估失败: {str(e)}",
                'risk_level': 'MEDIUM',
                'risk_score': 50
            }
    
    def _build_risk_prompt(self, state: Dict[str, Any]) -> str:
        """构建风险评估提示"""
        fundamentals = state.get('fundamentals_report', '暂无')
        market = state.get('market_report', '暂无')
        decision = state.get('decision', 'HOLD')
        investment_plan = state.get('investment_plan', '暂无')
        
        return f"""你是一名专业的风险控制专家。请对以下交易决策进行三维风险评估：

股票代码: {state.get('stock_code')}
股票名称: {state.get('stock_name', state.get('stock_code'))}
当前价格: {state.get('current_price', 'N/A')}元
建议决策: {decision}

=== 基本面摘要 ===
{fundamentals[:400]}...

=== 技术面摘要 ===
{market[:400]}...

=== 交易计划 ===
{investment_plan[:500]}...

请从以下三个维度进行风险评估：

**1. 保守维度（风险厌恶型投资者）**
- 最坏情况下能承受多少损失？
- 如果市场出现极端下跌（如-20%），该如何应对？
- 建议的风险控制措施

**2. 中性维度（理性投资者）**
- 基于当前信息的合理预期收益和风险比
- 胜率和赔率分析
- 关键的风险因素

**3. 激进维度（风险偏好型投资者）**
- 在高波动环境下如何最大化收益
- 杠杆使用建议
- 追涨杀跌的风险

请给出每种维度的具体风险评分（0-100，越高风险越大）和详细的风险分析。"""
    
    def _determine_risk_level(self, state: Dict[str, Any], risk_analysis: str) -> str:
        """确定风险等级"""
        analysis_upper = risk_analysis.upper()
        
        if '高风险' in risk_analysis or 'HIGH RISK' in analysis_upper or '风险较高' in risk_analysis:
            return 'HIGH'
        elif '低风险' in risk_analysis or 'LOW RISK' in analysis_upper or '风险较低' in risk_analysis:
            return 'LOW'
        else:
            return 'MEDIUM'
    
    def _calculate_risk_score(self, risk_level: str) -> int:
        """计算风险评分"""
        level_to_score = {
            'LOW': 30,
            'MEDIUM': 60,
            'HIGH': 85
        }
        return level_to_score.get(risk_level, 50)