package com.quant.market.service.impl;

import com.quant.market.adapter.DataSourceAdapter;
import com.quant.market.adapter.EastMoneyAdapter;
import com.quant.market.adapter.TushareAdapter;
import com.quant.market.cache.CacheManager;
import com.quant.market.config.MarketConfig;
import com.quant.market.exception.MarketException;
import com.quant.market.exception.MarketResultCode;
import com.quant.market.model.HistoricalKline;
import com.quant.market.model.RealtimeQuote;
import com.quant.market.model.TechnicalIndicators;
import com.quant.market.service.MarketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 市场服务实现类
 * 实现多数据源回退 + 多层缓存架构
 */
@Slf4j
@Service
public class MarketServiceImpl implements MarketService {
    
    @Autowired
    private MarketConfig marketConfig;
    
    @Autowired
    private TushareAdapter tushareAdapter;
    
    @Autowired
    private EastMoneyAdapter eastMoneyAdapter;
    
    @Autowired
    private CacheManager cacheManager;
    
    /** 数据源列表（按优先级） */
    private List<DataSourceAdapter> dataSources;
    
    @Autowired
    public void initDataSources(TushareAdapter tushare, EastMoneyAdapter eastMoney) {
        List<DataSourceAdapter> sources = new ArrayList<>();
        String[] priority = marketConfig.getDataSourcePriority().split(",");
        for (String name : priority) {
            name = name.trim().toLowerCase();
            if ("tushare".equals(name)) {
                sources.add(tushare);
            } else if ("eastmoney".equals(name)) {
                sources.add(eastMoney);
            }
        }
        // 如果配置为空，默认按Tushare、EastMoney顺序
        if (sources.isEmpty()) {
            sources.add(tushare);
            sources.add(eastMoney);
        }
        this.dataSources = sources;
    }
    
    // ==================== 原有方法实现（保持兼容） ====================
    
    @Override
    public Map<String, Object> getMarketData(String stockCode) {
        RealtimeQuote quote = getRealtimeQuote(stockCode);
        return convertToMap(quote);
    }
    
    @Override
    public BigDecimal getCurrentPrice(String stockCode) {
        try {
            RealtimeQuote quote = getRealtimeQuote(stockCode);
            return quote.getCurrentPrice();
        } catch (Exception e) {
            log.warn("获取当前价格失败: stockCode={}", stockCode, e);
            return null;
        }
    }
    
    // ==================== 新增方法实现 ====================
    
    @Override
    public RealtimeQuote getRealtimeQuote(String stockCode) {
        // 1. 尝试从缓存获取
        if (marketConfig.isCacheEnabled()) {
            RealtimeQuote cached = cacheManager.getRealtimeQuote(stockCode, RealtimeQuote.class);
            if (cached != null) {
                cached.setCached(true);
                log.debug("从缓存获取实时行情: {}", stockCode);
                return cached;
            }
        }
        
        // 2. 从数据源获取
        RealtimeQuote quote = fetchFromDataSource(ds -> ds.getRealtimeQuote(stockCode), "实时行情");
        
        // 3. 存入缓存
        if (quote != null && marketConfig.isCacheEnabled()) {
            quote.setCached(false);
            cacheManager.putRealtimeQuote(stockCode, quote);
        }
        
        return quote;
    }
    
    @Override
    public List<HistoricalKline> getHistoricalKline(String stockCode, String startDate, String endDate, String period) {
        // 默认时间范围：最近一年
        if (startDate == null || startDate.isEmpty()) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, -1);
            startDate = String.format("%tF", cal);
        }
        if (endDate == null || endDate.isEmpty()) {
            endDate = String.format("%tF", Calendar.getInstance());
        }
        if (period == null || period.isEmpty()) {
            period = "daily";
        }
        final String periodFinal = period;
        final String startDateFinal = startDate;
        final String endDateFinal = endDate;
        
        // 1. 尝试从缓存获取
        if (marketConfig.isCacheEnabled()) {
            List<HistoricalKline> cached = cacheManager.getHistoricalKline(stockCode, periodFinal, ArrayList.class);
            if (cached != null && !cached.isEmpty()) {
                // 检查缓存数据是否在时间范围内
                String cachedLatest = cached.get(cached.size() - 1).getTradeDate();
                if (cachedLatest.compareTo(startDateFinal) >= 0) {
                    log.debug("从缓存获取历史K线: {}", stockCode);
                    return cached;
                }
            }
        }
        
        // 2. 从数据源获取
        List<HistoricalKline> klines = fetchFromDataSource(ds -> ds.getHistoricalKline(stockCode, startDateFinal, endDateFinal, periodFinal), "历史K线");
        
        // 3. 存入缓存
        if (klines != null && !klines.isEmpty() && marketConfig.isCacheEnabled()) {
            cacheManager.putHistoricalKline(stockCode, periodFinal, (Serializable) klines);
        }
        
        return klines;
    }
    
    @Override
    public TechnicalIndicators getTechnicalIndicators(String stockCode, String period) {
        if (period == null || period.isEmpty()) {
            period = "daily";
        }
        final String periodFinal = period;
        
        // 1. 尝试从缓存获取
        if (marketConfig.isCacheEnabled()) {
            TechnicalIndicators cached = cacheManager.getTechnicalIndicators(stockCode, TechnicalIndicators.class);
            if (cached != null) {
                log.debug("从缓存获取技术指标: {}", stockCode);
                return cached;
            }
        }
        
        // 2. 从数据源获取
        TechnicalIndicators indicators = fetchFromDataSource(ds -> ds.getTechnicalIndicators(stockCode, periodFinal), "技术指标");
        
        // 3. 存入缓存
        if (indicators != null && marketConfig.isCacheEnabled()) {
            cacheManager.putTechnicalIndicators(stockCode, indicators);
        }
        
        return indicators;
    }
    
    @Override
    public List<RealtimeQuote> getBatchRealtimeQuote(List<String> stockCodes) {
        if (stockCodes == null || stockCodes.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<RealtimeQuote> quotes = new ArrayList<>();
        for (String stockCode : stockCodes) {
            try {
                RealtimeQuote quote = getRealtimeQuote(stockCode);
                if (quote != null) {
                    quotes.add(quote);
                }
            } catch (Exception e) {
                log.warn("批量获取行情失败: stockCode={}", stockCode, e);
            }
        }
        return quotes;
    }
    
    @Override
    public void refreshCache(String stockCode) {
        // 清除现有缓存
        cacheManager.evict("realtime:" + stockCode);
        cacheManager.evict("kline:" + stockCode + ":daily");
        cacheManager.evict("kline:" + stockCode + ":weekly");
        cacheManager.evict("kline:" + stockCode + ":monthly");
        cacheManager.evict("indicators:" + stockCode);
        log.info("缓存已刷新: {}", stockCode);
    }
    
    @Override
    public void clearCache() {
        cacheManager.clear();
        log.info("所有缓存已清空");
    }
    
    // ==================== 私有方法 ====================
    
    /**
     * 从数据源获取数据，支持多数据源回退
     */
    private <T> T fetchFromDataSource(DataSourceCallback<T> callback, String dataType) {
        List<String> triedSources = new ArrayList<>();
        
        for (DataSourceAdapter ds : dataSources) {
            triedSources.add(ds.getName());
            
            try {
                if (!ds.isAvailable()) {
                    log.warn("数据源不可用: {}", ds.getName());
                    continue;
                }
                
                T result = callback.execute(ds);
                if (result != null) {
                    log.info("从数据源获取{}成功: {}", dataType, ds.getName());
                    return result;
                }
            } catch (Exception e) {
                log.warn("从{}获取{}失败: {}", ds.getName(), dataType, e.getMessage());
            }
        }
        
        // 所有数据源都失败，尝试返回模拟数据
        if (marketConfig.isMockDataEnabled()) {
            log.warn("所有数据源都失败，返回模拟数据: {}", dataType);
            return getMockData(dataType);
        }
        
        throw new MarketException(MarketResultCode.DATA_SOURCE_UNAVAILABLE,
                String.format("无法获取%s，所有数据源都失败: %s", dataType, String.join(",", triedSources)));
    }
    
    /**
     * 将RealtimeQuote转换为Map（兼容旧接口）
     */
    private Map<String, Object> convertToMap(RealtimeQuote quote) {
        Map<String, Object> map = new HashMap<>();
        if (quote == null) {
            map.put("currentPrice", "--");
            return map;
        }
        
        map.put("stockCode", quote.getStockCode());
        map.put("stockName", quote.getStockName());
        map.put("currentPrice", quote.getCurrentPrice());
        map.put("highPrice", quote.getHigh());
        map.put("lowPrice", quote.getLow());
        map.put("openPrice", quote.getOpen());
        map.put("closePrice", quote.getCurrentPrice());
        map.put("changePercent", quote.getChangePercent());
        map.put("changeAmount", quote.getChange());
        map.put("volume", quote.getVolume());
        map.put("amount", quote.getAmount());
        map.put("bid1Price", quote.getBid1Price());
        map.put("ask1Price", quote.getAsk1Price());
        map.put("source", quote.getSource());
        map.put("cached", quote.getCached());
        
        return map;
    }
    
    /**
     * 获取模拟数据
     */
    @SuppressWarnings("unchecked")
    private <T> T getMockData(String dataType) {
        switch (dataType) {
            case "实时行情":
                RealtimeQuote mockQuote = new RealtimeQuote();
                mockQuote.setStockCode("600000");
                mockQuote.setStockName("浦发银行");
                mockQuote.setCurrentPrice(new BigDecimal("10.50"));
                mockQuote.setHigh(new BigDecimal("10.80"));
                mockQuote.setLow(new BigDecimal("10.30"));
                mockQuote.setOpen(new BigDecimal("10.40"));
                mockQuote.setPreviousClose(new BigDecimal("10.35"));
                mockQuote.setChange(new BigDecimal("0.15"));
                mockQuote.setChangePercent(new BigDecimal("1.45"));
                mockQuote.setVolume(50000000L);
                mockQuote.setAmount(new BigDecimal("525000000"));
                mockQuote.setSource("Mock");
                mockQuote.setCached(false);
                return (T) mockQuote;
                
            case "历史K线":
                List<HistoricalKline> mockKlines = new ArrayList<>();
                Calendar cal = Calendar.getInstance();
                for (int i = 0; i < 30; i++) {
                    HistoricalKline kline = new HistoricalKline();
                    kline.setStockCode("600000");
                    kline.setTradeDate(String.format("%tF", cal));
                    kline.setOpen(new BigDecimal("10.00"));
                    kline.setHigh(new BigDecimal("10.80"));
                    kline.setLow(new BigDecimal("9.80"));
                    kline.setClose(new BigDecimal("10.50"));
                    kline.setVolume(50000000L);
                    kline.setAmount(new BigDecimal("525000000"));
                    mockKlines.add(kline);
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                }
                return (T) mockKlines;
                
            case "技术指标":
                TechnicalIndicators mockIndicators = new TechnicalIndicators();
                mockIndicators.setStockCode("600000");
                mockIndicators.setMa5(new BigDecimal("10.20"));
                mockIndicators.setMa10(new BigDecimal("10.15"));
                mockIndicators.setMa20(new BigDecimal("10.00"));
                mockIndicators.setMa60(new BigDecimal("9.80"));
                mockIndicators.setRsi6(new BigDecimal("65.5"));
                mockIndicators.setRsi12(new BigDecimal("60.3"));
                mockIndicators.setRsi24(new BigDecimal("58.7"));
                mockIndicators.setMacd(new BigDecimal("0.25"));
                mockIndicators.setMacdSignal(new BigDecimal("0.20"));
                mockIndicators.setMacdHist(new BigDecimal("0.05"));
                mockIndicators.setKdjK(new BigDecimal("68.5"));
                mockIndicators.setKdjD(new BigDecimal("65.2"));
                mockIndicators.setKdjJ(new BigDecimal("75.1"));
                mockIndicators.setBollUpper(new BigDecimal("11.00"));
                mockIndicators.setBollMiddle(new BigDecimal("10.50"));
                mockIndicators.setBollLower(new BigDecimal("10.00"));
                return (T) mockIndicators;
                
            default:
                return null;
        }
    }
    
    @FunctionalInterface
    private interface DataSourceCallback<T> {
        T execute(DataSourceAdapter ds);
    }
}