"""
市场分析师 Agent
负责分析股票的技术面情况（K线、均线、技术指标等）
"""
from typing import Dict, Any
import logging

logger = logging.getLogger(__name__)


class MarketAnalyst:
    """市场分析师 - 分析K线形态、技术指标、趋势等"""
    
    def __init__(self, llm):
        self.llm = llm
    
    def analyze(self, state: Dict[str, Any]) -> Dict[str, Any]:
        """
        执行市场技术分析
        
        Args:
            state: AgentState，包含stock_code
        
        Returns:
            更新后的state，包含market_report
        """
        stock_code = state.get('stock_code', '')
        stock_name = state.get('stock_name', stock_code)
        
        logger.info(f"[市场分析师] 分析 {stock_code}")
        
        try:
            # 获取市场数据
            market_data = self._get_market_data(stock_code)
            
            # 构建分析提示
            prompt = self._build_analysis_prompt(stock_code, stock_name, market_data)
            
            # 调用LLM进行分析
            response = self.llm.invoke(prompt)
            analysis = str(response) if response else "市场分析无结果"
            
            return {
                'market_report': analysis,
                'market_data': market_data,
                'technical_indicators': market_data.get('indicators', {})
            }
            
        except Exception as e:
            logger.error(f"[市场分析师] 分析失败: {e}")
            return {
                'market_report': f"技术分析失败: {str(e)}",
                'market_score': 0
            }
    
    def _get_market_data(self, stock_code: str) -> Dict[str, Any]:
        """
        获取市场数据
        实际项目中应该调用真实的市场数据API
        """
        # 模拟市场数据
        return {
            'current_price': 15.85,
            'change_percent': 2.35,
            'volume': 15000000,
            'turnover_rate': 3.5,
            'high_52w': 22.50,
            'low_52w': 12.30,
            'ma5': 15.20,
            'ma10': 14.80,
            'ma20': 14.50,
            'ma60': 13.90,
            'indicators': {
                'rsi_14': 58.5,
                'macd': {'diff': 0.35, 'dea': 0.25, 'histogram': 0.10},
                'kdj': {'k': 65.2, 'd': 58.3, 'j': 79.0},
                'boll': {'upper': 16.50, 'middle': 15.20, 'lower': 13.90}
            },
            'trend': '上涨趋势',
            'support_level': 14.50,
            'resistance_level': 17.00
        }
    
    def _build_analysis_prompt(self, stock_code: str, stock_name: str,
                                market_data: Dict[str, Any]) -> str:
        """构建分析提示"""
        indicators = market_data.get('indicators', {})
        rsi = indicators.get('rsi_14', 'N/A')
        macd = indicators.get('macd', {})
        kdj = indicators.get('kdj', {})
        boll = indicators.get('boll', {})
        
        return f"""你是一名专业的技术分析师。请分析以下股票的技术面情况：

股票代码: {stock_code}
股票名称: {stock_name}

当前行情:
- 当前价格: {market_data.get('current_price', 'N/A')}元
- 涨跌幅: {market_data.get('change_percent', 'N/A')}%
- 成交量: {market_data.get('volume', 0) / 1000000:.2f}百万手
- 换手率: {market_data.get('turnover_rate', 'N/A')}%
- 52周最高: {market_data.get('high_52w', 'N/A')}元
- 52周最低: {market_data.get('low_52w', 'N/A')}元

均线系统:
- MA5: {market_data.get('ma5', 'N/A')}元
- MA10: {market_data.get('ma10', 'N/A')}元
- MA20: {market_data.get('ma20', 'N/A')}元
- MA60: {market_data.get('ma60', 'N/A')}元

技术指标:
- RSI(14): {rsi}
- MACD: DIFF={macd.get('diff', 'N/A'):.3f}, DEA={macd.get('dea', 'N/A'):.3f}, HIST={macd.get('histogram', 'N/A'):.3f}
- KDJ: K={kdj.get('k', 'N/A'):.1f}, D={kdj.get('d', 'N/A'):.1f}, J={kdj.get('j', 'N/A'):.1f}
- BOLL: 上轨={boll.get('upper', 'N/A')}, 中轨={boll.get('middle', 'N/A')}, 下轨={boll.get('lower', 'N/A')}

趋势判断: {market_data.get('trend', 'N/A')}
支撑位: {market_data.get('support_level', 'N/A')}元
阻力位: {market_data.get('resistance_level', 'N/A')}元

请从以下几个维度进行分析：
1. 趋势判断（当前趋势、均线排列）
2. 动能分析（RSI、MACD）
3. 趋势强度（KDJ、BOLL）
4. 关键价位分析（支撑、阻力）
5. 综合技术面评级

请用专业的技术分析语言，给出详细的技术分析报告。"""