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
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 东方财富 HTTP适配器（备用数据源）
 */
@Slf4j
@Component
public class EastMoneyAdapter implements DataSourceAdapter {
    
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String BASE_URL = "https://push2.eastmoney.com";
    
    @Autowired
    private MarketConfig marketConfig;
    
    private final OkHttpClient httpClient;
    private final Gson gson;
    
    public EastMoneyAdapter() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(marketConfig != null ? marketConfig.getHttpTimeout() : 10000, TimeUnit.MILLISECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }
    
    @Override
    public String getName() {
        return "EastMoney";
    }
    
    @Override
    public RealtimeQuote getRealtimeQuote(String stockCode) {
        try {
            String market = getMarketPrefix(stockCode);
            String url = BASE_URL + "/api/qt/stock/get";
            String params = String.format("fields=f43,f44,f45,f46,f47,f48,f50,f57,f58,f60,f107,f169,f170,f171&secid=%s.%s", 
                    market, stockCode);
            
            Request request = new Request.Builder()
                    .url(url + "?" + params)
                    .get()
                    .addHeader("Referer", "https://quote.eastmoney.com")
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new MarketException(MarketResultCode.NETWORK_ERROR, "EastMoney API请求失败: " + response.code());
                }
                String responseBody = response.body() != null ? response.body().string() : "";
                return parseRealtimeQuoteResponse(responseBody, stockCode);
            }
        } catch (MarketException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取东方财富实时行情失败: stockCode={}", stockCode, e);
            throw new MarketException(MarketResultCode.DATA_SOURCE_UNAVAILABLE, "EastMoney数据源不可用: " + e.getMessage());
        }
    }
    
    @Override
    public List<HistoricalKline> getHistoricalKline(String stockCode, String startDate, String endDate, String period) {
        try {
            String market = getMarketPrefix(stockCode);
            String url = "https://push2his.eastmoney.com/api/qt/stock/kline/get";
            String fields = "f43,f44,f45,f46,f47,f48,f50,f57,f58,f60,f107,f169,f170,f171";
            String params = String.format("fields1=f1,f2,f3,f4,f5,f6&fields2=%s&klt=101&fqt=1&secid=%s.%s&beg=%s&end=%s",
                    fields, market, stockCode, startDate.replace("-", ""), endDate.replace("-", ""));
            
            Request request = new Request.Builder()
                    .url(url + "?" + params)
                    .get()
                    .addHeader("Referer", "https://quote.eastmoney.com")
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new MarketException(MarketResultCode.NETWORK_ERROR, "EastMoney API请求失败: " + response.code());
                }
                String responseBody = response.body() != null ? response.body().string() : "";
                return parseHistoricalKlineResponse(responseBody, stockCode);
            }
        } catch (MarketException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取东方财富历史K线失败: stockCode={}", stockCode, e);
            throw new MarketException(MarketResultCode.DATA_SOURCE_UNAVAILABLE, "EastMoney数据源不可用: " + e.getMessage());
        }
    }
    
    @Override
    public TechnicalIndicators getTechnicalIndicators(String stockCode, String period) {
        // 复用TushareAdapter的计算逻辑，这里只获取K线数据
        List<HistoricalKline> klines = getHistoricalKline(stockCode, "", "", period);
        return calculateTechnicalIndicators(stockCode, klines);
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
            String url = BASE_URL + "/api/qt/stock/get?fields=f43&secid=1.600000";
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Referer", "https://quote.eastmoney.com")
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            log.warn("EastMoney可用性检查失败", e);
            return false;
        }
    }
    
    private String getMarketPrefix(String stockCode) {
        if (stockCode.startsWith("6") || stockCode.startsWith("9")) {
            return "1"; // 上海
        } else {
            return "0"; // 深圳
        }
    }
    
    private RealtimeQuote parseRealtimeQuoteResponse(String responseBody, String stockCode) {
        RealtimeQuote quote = new RealtimeQuote();
        quote.setStockCode(stockCode);
        quote.setSource("EastMoney");
        
        try {
            JsonObject data = gson.fromJson(responseBody, JsonObject.class);
            if (data.has("data") && !data.get("data").isJsonNull()) {
                JsonObject stockData = data.getAsJsonObject("data");
                
                // f43=最新价, f44=最高, f45=最低, f46=今开, f47=成交量, f48=成交额
                // f50=时间, f57=代码, f58=名称, f60=昨收, f169=买一价, f170=买一量
                // f107=涨跌额, f170=涨跌幅
                
                quote.setCurrentPrice(getDecimalField(stockData, "f43"));
                quote.setHigh(getDecimalField(stockData, "f44"));
                quote.setLow(getDecimalField(stockData, "f45"));
                quote.setOpen(getDecimalField(stockData, "f46"));
                quote.setVolume(getLongField(stockData, "f47"));
                quote.setAmount(getDecimalField(stockData, "f48"));
                quote.setPreviousClose(getDecimalField(stockData, "f60"));
                quote.setStockName(getStringField(stockData, "f58"));
                
                BigDecimal change = getDecimalField(stockData, "f107");
                BigDecimal changePercent = getDecimalField(stockData, "f170");
                quote.setChange(change);
                quote.setChangePercent(changePercent);
                
                // 买一价/量
                quote.setBid1Price(getDecimalField(stockData, "f169"));
                quote.setBid1Volume(getLongField(stockData, "f170"));
                
                // 卖一价/量  
                quote.setAsk1Price(getDecimalField(stockData, "f171"));
                
                // 判断市场
                if (stockCode.startsWith("6")) {
                    quote.setMarket("SH");
                } else {
                    quote.setMarket("SZ");
                }
            }
        } catch (Exception e) {
            log.error("解析东方财富实时行情响应失败", e);
            throw new MarketException(MarketResultCode.PARSE_ERROR, "数据解析失败: " + e.getMessage());
        }
        return quote;
    }
    
    private List<HistoricalKline> parseHistoricalKlineResponse(String responseBody, String stockCode) {
        List<HistoricalKline> klines = new ArrayList<>();
        try {
            JsonObject data = gson.fromJson(responseBody, JsonObject.class);
            if (data.has("data") && !data.get("data").isJsonNull()) {
                JsonObject klineData = data.getAsJsonObject("data");
                if (klineData.has("klines")) {
                    JsonArray klinesArray = klineData.getAsJsonArray("klines");
                    for (int i = 0; i < klinesArray.size(); i++) {
                        String[] parts = klinesArray.get(i).getAsString().split(",");
                        // 格式: 日期,开盘,收盘,最高,最低,成交量,成交额,振幅,涨跌幅,涨跌额,换手率
                        HistoricalKline kline = new HistoricalKline();
                        kline.setStockCode(stockCode);
                        kline.setTradeDate(parts[0]);
                        kline.setOpen(new BigDecimal(parts[1]));
                        kline.setClose(new BigDecimal(parts[2]));
                        kline.setHigh(new BigDecimal(parts[3]));
                        kline.setLow(new BigDecimal(parts[4]));
                        kline.setVolume(Long.parseLong(parts[5]));
                        kline.setAmount(new BigDecimal(parts[6]));
                        if (parts.length > 10) {
                            kline.setTurnoverRate(new BigDecimal(parts[10]));
                        }
                        klines.add(kline);
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析东方财富历史K线响应失败", e);
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
        
        if (size >= 5) indicators.setMa5(calculateMA(closes, 5));
        if (size >= 10) indicators.setMa10(calculateMA(closes, 10));
        if (size >= 20) indicators.setMa20(calculateMA(closes, 20));
        if (size >= 60) indicators.setMa60(calculateMA(closes, 60));
        
        if (size >= 6) indicators.setRsi6(calculateRSI(closes, 6));
        if (size >= 12) indicators.setRsi12(calculateRSI(closes, 12));
        if (size >= 24) indicators.setRsi24(calculateRSI(closes, 24));
        
        calculateMACD(closes, indicators);
        calculateKDJ(klines, indicators);
        calculateBollingerBands(closes, indicators);
        
        if (!klines.isEmpty()) {
            indicators.setDate(klines.get(klines.size() - 1).getTradeDate());
        }
        
        return indicators;
    }
    
    private BigDecimal calculateMA(List<BigDecimal> prices, int period) {
        if (prices.size() < period) return BigDecimal.ZERO;
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = prices.size() - period; i < prices.size(); i++) {
            sum = sum.add(prices.get(i));
        }
        return sum.divide(new BigDecimal(period), 2, java.math.RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateRSI(List<BigDecimal> prices, int period) {
        if (prices.size() < period + 1) return BigDecimal.ZERO;
        BigDecimal gains = BigDecimal.ZERO;
        BigDecimal losses = BigDecimal.ZERO;
        for (int i = prices.size() - period; i < prices.size(); i++) {
            BigDecimal change = prices.get(i).subtract(prices.get(i - 1));
            if (change.compareTo(BigDecimal.ZERO) > 0) gains = gains.add(change);
            else losses = losses.add(change.abs());
        }
        if (losses.compareTo(BigDecimal.ZERO) == 0) return new BigDecimal("100");
        BigDecimal rs = gains.divide(losses, 4, java.math.RoundingMode.HALF_UP);
        return new BigDecimal("100").subtract(new BigDecimal("100").divide(rs.add(BigDecimal.ONE), 4, java.math.RoundingMode.HALF_UP));
    }
    
    private void calculateMACD(List<BigDecimal> prices, TechnicalIndicators indicators) {
        if (prices.size() < 26) return;
        BigDecimal ema12 = calculateEMA(prices, 12);
        BigDecimal ema26 = calculateEMA(prices, 26);
        if (ema12 != null && ema26 != null) {
            BigDecimal dif = ema12.subtract(ema26);
            indicators.setMacd(dif);
            indicators.setMacdSignal(dif.multiply(new BigDecimal("0.9")));
            indicators.setMacdHist(dif.subtract(indicators.getMacdSignal()));
        }
    }
    
    private BigDecimal calculateEMA(List<BigDecimal> prices, int period) {
        if (prices.size() < period) return null;
        BigDecimal ema = prices.get(prices.size() - period);
        BigDecimal multiplier = new BigDecimal("2").divide(new BigDecimal(period + 1), 4, java.math.RoundingMode.HALF_UP);
        for (int i = prices.size() - period + 1; i < prices.size(); i++) {
            ema = prices.get(i).multiply(multiplier).add(ema.multiply(BigDecimal.ONE.subtract(multiplier)));
        }
        return ema;
    }
    
    private void calculateKDJ(List<HistoricalKline> klines, TechnicalIndicators indicators) {
        if (klines.size() < 9) return;
        int n = 9;
        BigDecimal maxHigh = klines.get(klines.size() - 1).getHigh();
        BigDecimal minLow = klines.get(klines.size() - 1).getLow();
        for (int i = klines.size() - n; i < klines.size(); i++) {
            if (klines.get(i).getHigh().compareTo(maxHigh) > 0) maxHigh = klines.get(i).getHigh();
            if (klines.get(i).getLow().compareTo(minLow) < 0) minLow = klines.get(i).getLow();
        }
        BigDecimal rsv = BigDecimal.ZERO;
        if (maxHigh.subtract(minLow).compareTo(BigDecimal.ZERO) != 0) {
            rsv = klines.get(klines.size() - 1).getClose().subtract(minLow)
                    .divide(maxHigh.subtract(minLow), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
        indicators.setKdjK(rsv.multiply(new BigDecimal("1/3")).add(new BigDecimal("50").multiply(new BigDecimal("2/3"))));
        indicators.setKdjD(indicators.getKdjK().multiply(new BigDecimal("1/3")).add(new BigDecimal("50").multiply(new BigDecimal("2/3"))));
        indicators.setKdjJ(indicators.getKdjK().multiply(new BigDecimal("3")).subtract(indicators.getKdjD().multiply(new BigDecimal("2"))));
    }
    
    private void calculateBollingerBands(List<BigDecimal> closes, TechnicalIndicators indicators) {
        if (closes.size() < 20) return;
        BigDecimal ma20 = calculateMA(closes, 20);
        indicators.setBollMiddle(ma20);
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
    
    private BigDecimal getDecimalField(JsonObject obj, String field) {
        if (obj.has(field) && !obj.get(field).isJsonNull()) {
            try {
                return new BigDecimal(obj.get(field).getAsString());
            } catch (Exception e) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }
    
    private Long getLongField(JsonObject obj, String field) {
        if (obj.has(field) && !obj.get(field).isJsonNull()) {
            try {
                return obj.get(field).getAsLong();
            } catch (Exception e) {
                return 0L;
            }
        }
        return 0L;
    }
    
    private String getStringField(JsonObject obj, String field) {
        if (obj.has(field) && !obj.get(field).isJsonNull()) {
            return obj.get(field).getAsString();
        }
        return "";
    }
}