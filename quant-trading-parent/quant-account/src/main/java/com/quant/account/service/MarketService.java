package com.quant.account.service;

import java.math.BigDecimal;
import java.util.Map;

public interface MarketService {

    /**
     * 获取标的实时行情
     * @param stockCode 证券代码
     * @return 行情数据
     */
    Map<String, Object> getMarketData(String stockCode);

    /**
     * 获取标的即时现价
     * @param stockCode 证券代码
     * @return 即时现价
     */
    BigDecimal getCurrentPrice(String stockCode);
}