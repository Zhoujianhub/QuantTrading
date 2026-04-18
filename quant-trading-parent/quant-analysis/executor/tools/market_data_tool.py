"""
市场数据工具类
用于获取股票的市场数据（K线、技术指标等）
"""
from typing import Dict, Any, List, Optional
import logging

logger = logging.getLogger(__name__)


class MarketDataTool:
    """市场数据获取工具"""
    
    def __init__(self, config: Optional[Dict[str, Any]] = None):
        self.config = config or {}
        self.data_source = self.config.get('data_source', 'mock')  # mock, tushare, akshare
    
    def get_realtime_quote(self, stock_code: str) -> Dict[str, Any]:
        """
        获取实时行情
        
        Args:
            stock_code: 股票代码，如 '000001.SZ'
        
        Returns:
            实时行情数据
        """
        logger.info(f"[MarketDataTool] 获取实时行情: {stock_code}")
        
        if self.data_source == 'mock':
            return self._mock_realtime_quote(stock_code)
        else:
            # TODO: 实现真实数据源调用
            return self._mock_realtime_quote(stock_code)
    
    def get_kline_data(self, stock_code: str, period: str = 'daily', 
                       start_date: str = None, end_date: str = None) -> List[Dict[str, Any]]:
        """
        获取K线数据
        
        Args:
            stock_code: 股票代码
            period: 周期 (daily/weekly/monthly)
            start_date: 开始日期
            end_date: 结束日期
        
        Returns:
            K线数据列表
        """
        logger.info(f"[MarketDataTool] 获取K线数据: {stock_code}, period={period}")
        
        if self.data_source == 'mock':
            return self._mock_kline_data(stock_code, period, start_date, end_date)
        else:
            return self._mock_kline_data(stock_code, period, start_date, end_date)
    
    def get_technical_indicators(self, stock_code: str) -> Dict[str, Any]:
        """
        获取技术指标
        
        Args:
            stock_code: 股票代码
        
        Returns:
            技术指标数据
        """
        logger.info(f"[MarketDataTool] 获取技术指标: {stock_code}")
        
        if self.data_source == 'mock':
            return self._mock_technical_indicators(stock_code)
        else:
            return self._mock_technical_indicators(stock_code)
    
    def get_market_summary(self, stock_code: str) -> Dict[str, Any]:
        """
        获取市场概况（行业涨跌幅、大盘指数等）
        """
        logger.info(f"[MarketDataTool] 获取市场概况: {stock_code}")
        return self._mock_market_summary(stock_code)
    
    def _mock_realtime_quote(self, stock_code: str) -> Dict[str, Any]:
        """模拟实时行情数据"""
        return {
            'stock_code': stock_code,
            'stock_name': self._get_stock_name(stock_code),
            'current_price': 15.85,
            'change': 0.36,
            'change_percent': 2.32,
            'open': 15.50,
            'high': 16.10,
            'low': 15.45,
            'volume': 15000000,
            'turnover_rate': 3.52,
            'pe': 15.5,
            'pb': 2.3,
            'market_cap': 100000000000,
            'timestamp': '2026-04-11 10:30:00'
        }
    
    def _mock_kline_data(self, stock_code: str, period: str,
                         start_date: str, end_date: str) -> List[Dict[str, Any]]:
        """模拟K线数据"""
        import random
        from datetime import datetime, timedelta
        
        klines = []
        base_price = 15.0
        days = 60 if period == 'daily' else 20
        
        for i in range(days):
            date = (datetime.now() - timedelta(days=days-i)).strftime('%Y-%m-%d')
            open_p = base_price + random.uniform(-0.5, 0.5)
            close_p = open_p + random.uniform(-0.3, 0.3)
            high_p = max(open_p, close_p) + random.uniform(0, 0.2)
            low_p = min(open_p, close_p) - random.uniform(0, 0.2)
            volume = random.randint(10000000, 20000000)
            
            klines.append({
                'date': date,
                'open': round(open_p, 2),
                'close': round(close_p, 2),
                'high': round(high_p, 2),
                'low': round(low_p, 2),
                'volume': volume,
                'turnover': round(volume * (open_p + close_p) / 2, 2)
            })
            
            base_price = close_p
        
        return klines
    
    def _mock_technical_indicators(self, stock_code: str) -> Dict[str, Any]:
        """模拟技术指标数据"""
        return {
            'ma': {
                'ma5': 15.20,
                'ma10': 14.80,
                'ma20': 14.50,
                'ma60': 13.90,
                'ma120': 13.50,
                'ma250': 13.00
            },
            'rsi': {
                'rsi6': 55.2,
                'rsi12': 58.5,
                'rsi24': 52.3
            },
            'macd': {
                'diff': 0.35,
                'dea': 0.25,
                'histogram': 0.10
            },
            'kdj': {
                'k': 65.2,
                'd': 58.3,
                'j': 79.0
            },
            'boll': {
                'upper': 16.50,
                'middle': 15.20,
                'lower': 13.90
            },
            'volume_ratio': 1.5,
            'turnover_rate': 3.52
        }
    
    def _mock_market_summary(self, stock_code: str) -> Dict[str, Any]:
        """模拟市场概况"""
        return {
            'index': {
                'sh000001': {'name': '上证指数', 'change': 0.45, 'change_percent': 1.2},
                'sz399001': {'name': '深证成指', 'change': 12.5, 'change_percent': 0.85},
                'cy399006': {'name': '创业板', 'change': -5.2, 'change_percent': -0.32}
            },
            'industry': {
                'industry_name': '新能源',
                'industry_change': 1.5,
                'lead_stock': stock_code
            }
        }
    
    def _get_stock_name(self, stock_code: str) -> str:
        """获取股票名称"""
        names = {
            '000001.SZ': '平安银行',
            '000002.SZ': '万科A',
            '600000.SH': '浦发银行',
            '600519.SH': '贵州茅台',
            '000858.SZ': '五粮液',
            '002594.SZ': '比亚迪'
        }
        return names.get(stock_code, stock_code)