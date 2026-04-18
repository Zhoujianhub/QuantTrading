package com.quant.market.service;

import com.quant.market.model.HistoricalKline;
import com.quant.market.model.RealtimeQuote;
import com.quant.market.model.TechnicalIndicators;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 市场服务接口
 */
public interface MarketService {
    
    // ==================== 原有方法（保持兼容） ====================
    
    /**
     * 获取标的实时行情（兼容旧接口）
     * @param stockCode 证券代码
     * @return 行情数据Map
     */
    Map<String, Object> getMarketData(String stockCode);
    
    /**
     * 获取标的即时现价
     * @param stockCode 证券代码
     * @return 即时现价
     */
    BigDecimal getCurrentPrice(String stockCode);
    
    // ==================== 新增方法 ====================
    
    /**
     * 获取实时行情
     * @param stockCode 股票代码
     * @return 实时行情数据
     */
    RealtimeQuote getRealtimeQuote(String stockCode);
    
    /**
     * 获取历史K线数据
     * @param stockCode 股票代码
     * @param startDate 开始日期（YYYY-MM-DD）
     * @param endDate 结束日期（YYYY-MM-DD）
     * @param period K线周期（daily/weekly/monthly）
     * @return 历史K线列表
     */
    List<HistoricalKline> getHistoricalKline(String stockCode, String startDate, String endDate, String period);
    
    /**
     * 获取技术指标
     * @param stockCode 股票代码
     * @param period 周期（daily/weekly/monthly）
     * @return 技术指标数据
     */
    TechnicalIndicators getTechnicalIndicators(String stockCode, String period);
    
    /**
     * 批量获取实时行情
     * @param stockCodes 股票代码列表
     * @return 实时行情列表
     */
    List<RealtimeQuote> getBatchRealtimeQuote(List<String> stockCodes);
    
    /**
     * 刷新指定股票缓存
     * @param stockCode 股票代码
     */
    void refreshCache(String stockCode);
    
    /**
     * 清空所有缓存
     */
    void clearCache();
}