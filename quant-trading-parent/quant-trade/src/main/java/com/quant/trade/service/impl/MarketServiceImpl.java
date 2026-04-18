package com.quant.trade.service.impl;

import com.quant.trade.service.MarketService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class MarketServiceImpl implements MarketService {

    @Override
    public Map<String, Object> getMarketData(String stockCode) {
        Map<String, Object> marketData = new HashMap<>();
        try {
            // 这里应该调用AKShare或BaoStock接口获取真实行情数据
            // 目前模拟返回测试数据
            marketData.put("currentPrice", new BigDecimal("62.47"));
            marketData.put("highPrice", new BigDecimal("63.00"));
            marketData.put("lowPrice", new BigDecimal("61.50"));
            marketData.put("openPrice", new BigDecimal("62.00"));
            marketData.put("closePrice", new BigDecimal("62.47"));
            marketData.put("changePercent", new BigDecimal("0.27"));
            marketData.put("changeAmount", new BigDecimal("+0.43%"));
            marketData.put("buy1", new BigDecimal("62.46"));
            marketData.put("buy2", new BigDecimal("62.45"));
            marketData.put("buy3", new BigDecimal("62.44"));
            marketData.put("buy4", new BigDecimal("62.43"));
            marketData.put("buy5", new BigDecimal("62.42"));
            marketData.put("sell1", new BigDecimal("62.48"));
            marketData.put("sell2", new BigDecimal("62.49"));
            marketData.put("sell3", new BigDecimal("62.50"));
            marketData.put("sell4", new BigDecimal("62.51"));
            marketData.put("sell5", new BigDecimal("62.52"));
            marketData.put("limitUp", new BigDecimal("68.72"));
            marketData.put("limitDown", new BigDecimal("56.22"));
        } catch (Exception e) {
            // 降级机制，如果所有行情源都失败，返回默认值
            marketData.put("currentPrice", "--");
            marketData.put("highPrice", "--");
            marketData.put("lowPrice", "--");
            marketData.put("openPrice", "--");
            marketData.put("closePrice", "--");
            marketData.put("changePercent", "--");
            marketData.put("changeAmount", "--");
            marketData.put("buy1", "--");
            marketData.put("buy2", "--");
            marketData.put("buy3", "--");
            marketData.put("buy4", "--");
            marketData.put("buy5", "--");
            marketData.put("sell1", "--");
            marketData.put("sell2", "--");
            marketData.put("sell3", "--");
            marketData.put("sell4", "--");
            marketData.put("sell5", "--");
            marketData.put("limitUp", "--");
            marketData.put("limitDown", "--");
        }
        return marketData;
    }

    @Override
    public BigDecimal getCurrentPrice(String stockCode) {
        Map<String, Object> marketData = getMarketData(stockCode);
        Object currentPrice = marketData.get("currentPrice");
        if (currentPrice instanceof BigDecimal) {
            return (BigDecimal) currentPrice;
        }
        return null;
    }
}