package com.quant.market.controller;

import com.quant.market.exception.MarketException;
import com.quant.market.exception.MarketResultCode;
import com.quant.market.model.HistoricalKline;
import com.quant.market.model.RealtimeQuote;
import com.quant.market.model.TechnicalIndicators;
import com.quant.market.service.MarketService;
import com.quant.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 市场行情控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/market")
public class MarketController {
    
    @Autowired
    private MarketService marketService;
    
    // ==================== 兼容旧接口 ====================
    
    @GetMapping("/data/{stockCode}")
    public Result<Map<String, Object>> getMarketData(@PathVariable String stockCode) {
        try {
            Map<String, Object> marketData = marketService.getMarketData(stockCode);
            return Result.success(marketData);
        } catch (MarketException e) {
            log.error("获取行情数据失败: stockCode={}", stockCode, e);
            return Result.error(e.getErrorCode(), e.getErrorMessage());
        } catch (Exception e) {
            log.error("获取行情数据异常: stockCode={}", stockCode, e);
            return Result.error(MarketResultCode.UNKNOWN_ERROR.getCode(), e.getMessage());
        }
    }
    
    @GetMapping("/price/{stockCode}")
    public Result<String> getCurrentPrice(@PathVariable String stockCode) {
        try {
            var price = marketService.getCurrentPrice(stockCode);
            return Result.success(price != null ? price.toString() : "--");
        } catch (Exception e) {
            return Result.success("--");
        }
    }
    
    // ==================== 新接口 ====================
    
    /**
     * 获取实时行情
     */
    @GetMapping("/realtime/{stockCode}")
    public Result<RealtimeQuote> getRealtimeQuote(@PathVariable String stockCode) {
        try {
            RealtimeQuote quote = marketService.getRealtimeQuote(stockCode);
            return Result.success(quote);
        } catch (MarketException e) {
            log.error("获取实时行情失败: stockCode={}", stockCode, e);
            return Result.error(e.getErrorCode(), e.getErrorMessage());
        } catch (Exception e) {
            log.error("获取实时行情异常: stockCode={}", stockCode, e);
            return Result.error(MarketResultCode.UNKNOWN_ERROR.getCode(), e.getMessage());
        }
    }
    
    /**
     * 获取历史K线
     */
    @GetMapping("/kline/{stockCode}")
    public Result<List<HistoricalKline>> getHistoricalKline(
            @PathVariable String stockCode,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "daily") String period) {
        try {
            List<HistoricalKline> klines = marketService.getHistoricalKline(stockCode, startDate, endDate, period);
            return Result.success(klines);
        } catch (MarketException e) {
            log.error("获取历史K线失败: stockCode={}", stockCode, e);
            return Result.error(e.getErrorCode(), e.getErrorMessage());
        } catch (Exception e) {
            log.error("获取历史K线异常: stockCode={}", stockCode, e);
            return Result.error(MarketResultCode.UNKNOWN_ERROR.getCode(), e.getMessage());
        }
    }
    
    /**
     * 获取技术指标
     */
    @GetMapping("/indicators/{stockCode}")
    public Result<TechnicalIndicators> getTechnicalIndicators(
            @PathVariable String stockCode,
            @RequestParam(required = false, defaultValue = "daily") String period) {
        try {
            TechnicalIndicators indicators = marketService.getTechnicalIndicators(stockCode, period);
            return Result.success(indicators);
        } catch (MarketException e) {
            log.error("获取技术指标失败: stockCode={}", stockCode, e);
            return Result.error(e.getErrorCode(), e.getErrorMessage());
        } catch (Exception e) {
            log.error("获取技术指标异常: stockCode={}", stockCode, e);
            return Result.error(MarketResultCode.UNKNOWN_ERROR.getCode(), e.getMessage());
        }
    }
    
    /**
     * 批量获取实时行情
     */
    @PostMapping("/batch/realtime")
    public Result<List<RealtimeQuote>> getBatchRealtimeQuote(@RequestBody List<String> stockCodes) {
        try {
            if (stockCodes == null || stockCodes.isEmpty()) {
                return Result.error(MarketResultCode.PARAM_INVALID.getCode(), "股票代码列表不能为空");
            }
            List<RealtimeQuote> quotes = marketService.getBatchRealtimeQuote(stockCodes);
            return Result.success(quotes);
        } catch (MarketException e) {
            log.error("批量获取实时行情失败", e);
            return Result.error(e.getErrorCode(), e.getErrorMessage());
        } catch (Exception e) {
            log.error("批量获取实时行情异常", e);
            return Result.error(MarketResultCode.UNKNOWN_ERROR.getCode(), e.getMessage());
        }
    }
    
    // ==================== 缓存管理接口 ====================
    
    /**
     * 刷新指定股票缓存
     */
    @DeleteMapping("/cache/{stockCode}")
    public Result<Void> refreshCache(@PathVariable String stockCode) {
        try {
            marketService.refreshCache(stockCode);
            return Result.success(null);
        } catch (Exception e) {
            log.error("刷新缓存失败: stockCode={}", stockCode, e);
            return Result.error(MarketResultCode.CACHE_ERROR.getCode(), e.getMessage());
        }
    }
    
    /**
     * 清空所有缓存
     */
    @DeleteMapping("/cache")
    public Result<Void> clearCache() {
        try {
            marketService.clearCache();
            return Result.success(null);
        } catch (Exception e) {
            log.error("清空缓存失败", e);
            return Result.error(MarketResultCode.CACHE_ERROR.getCode(), e.getMessage());
        }
    }
    
    /**
     * 获取缓存状态
     */
    @GetMapping("/cache/stats")
    public Result<Map<String, Object>> getCacheStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("status", "running");
            stats.put("message", "缓存统计功能开发中");
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(MarketResultCode.UNKNOWN_ERROR.getCode(), e.getMessage());
        }
    }
}