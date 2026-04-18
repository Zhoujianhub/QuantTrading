package com.quant.trade.controller;

import com.quant.trade.service.MarketService;
import com.quant.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/market")
public class MarketController {

    @Autowired
    private MarketService marketService;

    @GetMapping("/data/{stockCode}")
    public Result<Map<String, Object>> getMarketData(@PathVariable String stockCode) {
        Map<String, Object> marketData = marketService.getMarketData(stockCode);
        return Result.success(marketData);
    }

    @GetMapping("/price/{stockCode}")
    public Result<String> getCurrentPrice(@PathVariable String stockCode) {
        try {
            return Result.success(marketService.getCurrentPrice(stockCode).toString());
        } catch (Exception e) {
            return Result.success("--");
        }
    }
}