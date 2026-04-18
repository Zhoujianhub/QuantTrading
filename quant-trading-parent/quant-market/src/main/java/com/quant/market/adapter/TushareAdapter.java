package com.quant.market.adapter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.quant.market.config.MarketConfig;
import com.quant.market.exception.MarketException;
import com.quant.market.exception.MarketResultCode;
import com.quant.market.model.HistoricalKline;
import com.quant.market.model.RealtimeQuote;
import com.quant.market.model.TechnicalIndicators;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Tushare Pro API HTTP适配器
 */
@Slf4j
@Component
public class TushareAdapter implements DataSourceAdapter {
    
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    @Autowired
    private MarketConfig marketConfig;
    
    private final OkHttpClient httpClient;
    private final Gson gson;
    
    public TushareAdapter() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(marketConfig != null ? marketConfig.getHttpTimeout() : 10000, TimeUnit.MILLISECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }
    
    @Override
    public String getName() {
        return "Tushare";
    }
    
    @Override
    public RealtimeQuote getRealtimeQuote(String stockCode) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("api_name", "stock_basic");
            params.put("token", marketConfig.getTushareToken());
            params.put("params", Map.of("ts_code", stockCode, "list_status", "L"));
            params.put("fields", "ts_code,name,close,high,low,open,pre_close,vol,amount");
            
            JsonObject response = callApi(params);
            return parseRealtimeQuoteResponse(response, stockCode);
        } catch (Exception e) {
            log.error("获取Tushare实时行情失败: stockCode={}", stockCode, e);
            throw new MarketException(MarketResultCode.DATA_SOURCE_UNAVAILABLE, "Tushare数据源不可用: " + e.getMessage());
        }
    }
    
    @Override
    public List<HistoricalKline> getHistoricalKline(String stockCode, String startDate, String endDate, String period) {
        try {
            Map<String, Object> params = new HashMap<>();
            String apiName = "daily".equals(period) ? "daily" : "weekly".equals(period) ? "weekly" : "monthly";
            params.put("api_name", apiName);
            params.put("token", marketConfig.getTushareToken());
            params.put("params", Map.of("ts_code", stockCode, "start_date", startDate, "end_date", endDate));
            params.put("fields", "ts_code,trade_date,open,high,low,close,vol,amount,turnover_rate");
            
            JsonObject response = callApi(params);
            return parseHistoricalKlineResponse(response, stockCode);
        } catch (Exception e) {
            log.error("获取Tushare历史K线失败: stockCode={}", stockCode, e);
            throw new MarketException(MarketResultCode.DATA_SOURCE_UNAVAILABLE, "Tushare数据源不可用: " + e.getMessage());
        }
    }
    
    @Override
    public TechnicalIndicators getTechnicalIndicators(String stockCode, String period) {
        try {
            // 获取日线数据计算技术指标
            List<HistoricalKline> klines = getHistoricalKline(stockCode, "", "", period);
            return calculateTechnicalIndicators(stockCode, klines);
        } catch (Exception e) {
            log.error("获取技术指标失败: stockCode={}", stockCode, e);
            throw new MarketException(MarketResultCode.DATA_SOURCE_UNAVAILABLE, "Tushare数据源不可用: " + e.getMessage());
        }
    }
    
    @Override
    public List<RealtimeQuote> getBatchRealtimeQuote(List<String> stockCodes) {
        List<RealtimeQuote> quotes = new ArrayList<>();
        for (String stockCode : stockCodes) {
            try {
                quotes.add(getRealtimeQuote(stockCode));
            } catch (Exception e) {
                log.warn("批量获取行情失败: stockCode={}", stockCode, e);
            }
        }
        return quotes;
    }
    
    @Override
    public boolean isAvailable() {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("api_name", "trade_cal");
            params.put("token", marketConfig.getTushareToken());
            params.put("params", Map.of("exchange", "SSE", "start_date", "2026-01-01", "end_date", "2026-01-01"));
            params.put("fields", "");
            callApi(params);
            return true;
        } catch (Exception e) {
            log.warn("Tushare可用性检查失败", e);
            return false;
        }
    }
    
    private JsonObject callApi(Map<String, Object> params) throws Exception {
        String json = gson.toJson(params);
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(marketConfig.getTushareUrl())
                .post(body)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new MarketException(MarketResultCode.NETWORK_ERROR, "Tushare API请求失败: " + response.code());
            }
            String responseBody = response.body() != null ? response.body().string() : "";
            JsonObject result = gson.fromJson(responseBody, JsonObject.class);
            
            if (result.has("code") && result.get("code").getAsInt() != 0) {
                String msg = result.has("msg") ? result.get("msg").getAsString() : "Unknown error";
                throw new MarketException(MarketResultCode.DATA_SOURCE_UNAVAILABLE, "Tushare API错误: " + msg);
            }
            return result;
        }
    }
    
    private RealtimeQuote parseRealtimeQuoteResponse(JsonObject response, String stockCode) {
        RealtimeQuote quote = new RealtimeQuote();
        quote.setStockCode(stockCode);
        quote.setSource("Tushare");
        
        try {
            if (response.has("data")) {
                JsonObject data = response.getAsJsonObject("data");
                if (data.has("items") && data.getAsJsonArray("items").size() > 0) {
                    JsonArray item = data.getAsJsonArray("items").get(0).getAsJsonArray();
                    // 根据fields顺序解析: ts_code,name,close,high,low,open,pre_close,vol,amount
                    if (item.size() >= 9) {
                        quote.setStockCode(item.get(0).getAsString());
                        quote.setStockName(item.get(1).getAsString());
                        quote.setCurrentPrice(new BigDecimal(item.get(2).getAsString()));
                        quote.setHigh(new BigDecimal(item.get(3).getAsString()));
                        quote.setLow(new BigDecimal(item.get(4).getAsString()));
                        quote.setOpen(new BigDecimal(item.get(5).getAsString()));
                        quote.setPreviousClose(new BigDecimal(item.get(6).getAsString()));
                        quote.setVolume(item.get(7).getAsLong());
                        quote.setAmount(new BigDecimal(item.get(8).getAsString()));
                        
                        // 计算涨跌额和涨跌幅
                        BigDecimal change = quote.getCurrentPrice().subtract(quote.getPreviousClose());
                        quote.setChange(change);
                        if (quote.getPreviousClose().compareTo(BigDecimal.ZERO) > 0) {
                            quote.setChangePercent(change.divide(quote.getPreviousClose(), 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100")));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析Tushare实时行情响应失败", e);
            throw new MarketException(MarketResultCode.PARSE_ERROR, "数据解析失败: " + e.getMessage());
        }
        return quote;
    }
    
    private List<HistoricalKline> parseHistoricalKlineResponse(JsonObject response, String stockCode) {
        List<HistoricalKline> klines = new ArrayList<>();
        try {
            if (response.has("data")) {
                JsonObject data = response.getAsJsonObject("data");
                if (data.has("items")) {
                    JsonArray items = data.getAsJsonArray("items");
                    for (int i = 0; i < items.size(); i++) {
                        JsonArray item = items.get(i).getAsJsonArray();
                        // fields: ts_code,trade_date,open,high,low,close,vol,amount,turnover_rate
                        HistoricalKline kline = new HistoricalKline();
                        kline.setStockCode(item.get(0).getAsString());
                        kline.setTradeDate(item.get(1).getAsString());
                        kline.setOpen(new BigDecimal(item.get(2).getAsString()));
                        kline.setHigh(new BigDecimal(item.get(3).getAsString()));
                        kline.setLow(new BigDecimal(item.get(4).getAsString()));
                        kline.setClose(new BigDecimal(item.get(5).getAsString()));
                        kline.setVolume(item.get(6).getAsLong());
                        kline.setAmount(new BigDecimal(item.get(7).getAsString()));
                        if (item.size() > 8) {
                            kline.setTurnoverRate(new BigDecimal(item.get(8).getAsString()));
                        }
                        klines.add(kline);
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析Tushare历史K线响应失败", e);
            throw new MarketException(MarketResultCode.PARSE_ERROR, "数据解析失败: " + e.getMessage());
        }
        return klines;
    }
    
    private TechnicalIndicators calculateTechnicalIndicators(String stockCode, List<HistoricalKline> klines) {
        TechnicalIndicators indicators = new TechnicalIndicators();
        indicators.setStockCode(stockCode);
        
        if (klines == null || klines.isEmpty()) {
            return indicators;
        }
        
        int size = klines.size();
        List<BigDecimal> closes = new ArrayList<>();
        for (HistoricalKline k : klines) {
            closes.add(k.getClose());
        }
        
        // 计算MA
        if (size >= 5) {
            indicators.setMa5(calculateMA(closes, 5));
        }
        if (size >= 10) {
            indicators.setMa10(calculateMA(closes, 10));
        }
        if (size >= 20) {
            indicators.setMa20(calculateMA(closes, 20));
        }
        if (size >= 60) {
            indicators.setMa60(calculateMA(closes, 60));
        }
        
        // 计算RSI
        if (size >= 6) {
            indicators.setRsi6(calculateRSI(closes, 6));
        }
        if (size >= 12) {
            indicators.setRsi12(calculateRSI(closes, 12));
        }
        if (size >= 24) {
            indicators.setRsi24(calculateRSI(closes, 24));
        }
        
        // 计算MACD
        calculateMACD(closes, indicators);
        
        // 计算KDJ
        calculateKDJ(klines, indicators);
        
        // 计算布林带
        calculateBollingerBands(closes, indicators);
        
        if (!klines.isEmpty()) {
            indicators.setDate(klines.get(klines.size() - 1).getTradeDate());
        }
        
        return indicators;
    }
    
    private BigDecimal calculateMA(List<BigDecimal> prices, int period) {
        if (prices.size() < period) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = prices.size() - period; i < prices.size(); i++) {
            sum = sum.add(prices.get(i));
        }
        return sum.divide(new BigDecimal(period), 2, java.math.RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateRSI(List<BigDecimal> prices, int period) {
        if (prices.size() < period + 1) {
            return BigDecimal.ZERO;
        }
        BigDecimal gains = BigDecimal.ZERO;
        BigDecimal losses = BigDecimal.ZERO;
        for (int i = prices.size() - period; i < prices.size(); i++) {
            BigDecimal change = prices.get(i).subtract(prices.get(i - 1));
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                gains = gains.add(change);
            } else {
                losses = losses.add(change.abs());
            }
        }
        if (losses.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal("100");
        }
        BigDecimal rs = gains.divide(losses, 4, java.math.RoundingMode.HALF_UP);
        return new BigDecimal("100").subtract(new BigDecimal("100").divide(rs.add(BigDecimal.ONE), 4, java.math.RoundingMode.HALF_UP));
    }
    
    private void calculateMACD(List<BigDecimal> prices, TechnicalIndicators indicators) {
        if (prices.size() < 12) {
            return;
        }
        BigDecimal ema12 = calculateEMA(prices, 12);
        BigDecimal ema26 = calculateEMA(prices, 26);
        if (ema12 != null && ema26 != null) {
            BigDecimal dif = ema12.subtract(ema26);
            indicators.setMacd(dif);
            // Signal线为DIF的9日EMA，这里简化处理
            indicators.setMacdSignal(dif.multiply(new BigDecimal("0.9")));
            indicators.setMacdHist(dif.subtract(indicators.getMacdSignal()));
        }
    }
    
    private BigDecimal calculateEMA(List<BigDecimal> prices, int period) {
        if (prices.size() < period) {
            return null;
        }
        BigDecimal ema = prices.get(prices.size() - period);
        BigDecimal multiplier = new BigDecimal("2").divide(new BigDecimal(period + 1), 4, java.math.RoundingMode.HALF_UP);
        for (int i = prices.size() - period + 1; i < prices.size(); i++) {
            ema = prices.get(i).multiply(multiplier).add(ema.multiply(BigDecimal.ONE.subtract(multiplier)));
        }
        return ema;
    }
    
    private void calculateKDJ(List<HistoricalKline> klines, TechnicalIndicators indicators) {
        if (klines.size() < 9) {
            return;
        }
        int n = 9;
        BigDecimal maxHigh = klines.get(klines.size() - 1).getHigh();
        BigDecimal minLow = klines.get(klines.size() - 1).getLow();
        for (int i = klines.size() - n; i < klines.size(); i++) {
            if (klines.get(i).getHigh().compareTo(maxHigh) > 0) {
                maxHigh = klines.get(i).getHigh();
            }
            if (klines.get(i).getLow().compareTo(minLow) < 0) {
                minLow = klines.get(i).getLow();
            }
        }
        BigDecimal rsv = BigDecimal.ZERO;
        if (maxHigh.subtract(minLow).compareTo(BigDecimal.ZERO) != 0) {
            rsv = klines.get(klines.size() - 1).getClose().subtract(minLow)
                    .divide(maxHigh.subtract(minLow), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
        BigDecimal k = new BigDecimal("50");
        BigDecimal d = new BigDecimal("50");
        indicators.setKdjK(rsv.multiply(new BigDecimal("1/3")).add(k.multiply(new BigDecimal("2/3"))));
        indicators.setKdjD(indicators.getKdjK().multiply(new BigDecimal("1/3")).add(d.multiply(new BigDecimal("2/3"))));
        indicators.setKdjJ(indicators.getKdjK().multiply(new BigDecimal("3")).subtract(indicators.getKdjD().multiply(new BigDecimal("2"))));
    }
    
    private void calculateBollingerBands(List<BigDecimal> closes, TechnicalIndicators indicators) {
        if (closes.size() < 20) {
            return;
        }
        BigDecimal ma20 = calculateMA(closes, 20);
        indicators.setBollMiddle(ma20);
        
        // 计算20日标准差
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = closes.size() - 20; i < closes.size(); i++) {
            sum = sum.add(closes.get(i));
        }
        BigDecimal mean = sum.divide(new BigDecimal("20"), 4, java.math.RoundingMode.HALF_UP);
        BigDecimal variance = BigDecimal.ZERO;
        for (int i = closes.size() - 20; i < closes.size(); i++) {
            variance = variance.add(closes.get(i).subtract(mean).pow(2));
        }
        BigDecimal stdDev = new BigDecimal(Math.sqrt(variance.divide(new BigDecimal("20"), 4, java.math.RoundingMode.HALF_UP).doubleValue()));
        
        indicators.setBollUpper(ma20.add(stdDev.multiply(new BigDecimal("2"))));
        indicators.setBollLower(ma20.subtract(stdDev.multiply(new BigDecimal("2"))));
    }
}