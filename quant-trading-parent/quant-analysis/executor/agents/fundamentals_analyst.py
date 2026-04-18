"""
基本面分析师 Agent
负责分析股票的财务数据和基本面情况
"""
from typing import Dict, Any
import logging

logger = logging.getLogger(__name__)


class FundamentalsAnalyst:
    """基本面分析师 - 分析财务数据、估值、盈利能力等"""
    
    def __init__(self, llm):
        self.llm = llm
    
    def analyze(self, state: Dict[str, Any]) -> Dict[str, Any]:
        """
        执行基本面分析
        
        Args:
            state: AgentState，包含stock_code
        
        Returns:
            更新后的state，包含fundamentals_report
        """
        stock_code = state.get('stock_code', '')
        stock_name = state.get('stock_name', stock_code)
        
        logger.info(f"[基本面分析师] 分析 {stock_code}")
        
        try:
            # 获取财务数据（这里应该调用真实的数据API）
            financial_data = self._get_financial_data(stock_code)
            
            # 构建分析提示
            prompt = self._build_analysis_prompt(stock_code, stock_name, financial_data)
            
            # 调用LLM进行分析
            response = self.llm.invoke(prompt)
            analysis = str(response) if response else "基本面分析无结果"
            
            return {
                'fundamentals_report': analysis,
                'financial_data': financial_data,
                'analysis_timestamp': state.get('analysis_timestamp', '')
            }
            
        except Exception as e:
            logger.error(f"[基本面分析师] 分析失败: {e}")
            return {
                'fundamentals_report': f"基本面分析失败: {str(e)}",
                'fundamentals_score': 0
            }
    
    def _get_financial_data(self, stock_code: str) -> Dict[str, Any]:
        """
        获取财务数据
        实际项目中应该调用真实的数据源API
        """
        # 模拟财务数据
        return {
            'pe_ratio': 15.5,
            'pb_ratio': 2.3,
            'roe': 12.5,
            'revenue_growth': 8.2,
            'profit_growth': 10.3,
            'debt_ratio': 45.2,
            'current_ratio': 2.1,
            'dividend_yield': 2.5,
            'market_cap': 100000000000,  # 总市值
            'float_ratio': 65.0,  # 流通比例
        }
    
    def _build_analysis_prompt(self, stock_code: str, stock_name: str, 
                               financial_data: Dict[str, Any]) -> str:
        """构建分析提示"""
        return f"""你是一名专业的基本面分析师。请分析以下股票的基本面情况：

股票代码: {stock_code}
股票名称: {stock_name}

财务数据:
- 市盈率(PE): {financial_data.get('pe_ratio', 'N/A')}
- 市净率(PB): {financial_data.get('pb_ratio', 'N/A')}
- 净资产收益率(ROE): {financial_data.get('roe', 'N/A')}%
- 营收增长率: {financial_data.get('revenue_growth', 'N/A')}%
- 利润增长率: {financial_data.get('profit_growth', 'N/A')}%
- 资产负债率: {financial_data.get('debt_ratio', 'N/A')}%
- 流动比率: {financial_data.get('current_ratio', 'N/A')}
- 股息收益率: {financial_data.get('dividend_yield', 'N/A')}%
- 总市值: {financial_data.get('market_cap', 0) / 100000000:.2f}亿元

请从以下几个维度进行分析：
1. 估值水平分析（PE、PB是否合理）
2. 盈利能力分析（ROE、利润增长率）
3. 财务健康度（资产负债率、流动比率）
4. 成长性评估
5. 投资价值总结

请用专业的分析语言，给出详细的基本面分析报告。"""