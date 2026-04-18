package com.quant.market.adapter;

import com.quant.market.model.HistoricalKline;
import com.quant.market.model.RealtimeQuote;
import com.quant.market.model.TechnicalIndicators;

import java.util.List;

/**
 * 数据源适配器接口
 * 定义获取行情数据的标准方法
 */
public interface DataSourceAdapter {
    
    /**
     * 获取数据源名称
     * @return 数据源名称
     */
    String getName();
    
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
     * 检查数据源是否可用
     * @return true表示可用
     */
    boolean isAvailable();
}