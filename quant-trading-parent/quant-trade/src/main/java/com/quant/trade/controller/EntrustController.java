package com.quant.trade.controller;

import com.quant.trade.entity.Entrust;
import com.quant.trade.service.EntrustService;
import com.quant.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/entrust")
public class EntrustController {

    @Autowired
    private EntrustService entrustService;

    @Autowired
    private RestTemplate restTemplate;

    private static final String HOLDING_SERVICE_URL = "http://localhost:8085/api/v1/holdings";

    @PostMapping("/create")
    public Result<String> createEntrust(@RequestBody Entrust entrust) {
        try {
            String entrustNo = entrustService.createEntrust(entrust);
            try {
                BigDecimal price = entrust.getEntrustPrice() != null 
                    ? entrust.getEntrustPrice().divide(new BigDecimal(100))
                    : BigDecimal.ZERO;
                
                String accountId = entrust.getClientId() != null ? entrust.getClientId() : "TEST001";
                
                String syncUrl = HOLDING_SERVICE_URL + "/sync-from-entrust" 
                    + "?accountId=" + accountId
                    + "&stockCode=" + entrust.getStockCode()
                    + "&stockName=" + entrust.getStockName()
                    + "&entrustBs=" + entrust.getEntrustBs()
                    + "&entrustAmount=" + entrust.getEntrustAmount().intValue()
                    + "&entrustPrice=" + price;
                
                restTemplate.postForObject(syncUrl, null, String.class);
                log.info("持仓同步成功: accountId={}, stockCode={}", accountId, entrust.getStockCode());
            } catch (Exception e) {
                log.warn("持仓同步失败: {}", e.getMessage());
            }
            return Result.success(entrustNo);
        } catch (Exception e) {
            return Result.error("下单失败：" + e.getMessage());
        }
    }

    @GetMapping("/client/{clientId}")
    public Result<List<Entrust>> getEntrustsByClientId(@PathVariable String clientId) {
        List<Entrust> entrusts = entrustService.getEntrustsByClientId(clientId);
        return Result.success(entrusts);
    }

    @DeleteMapping("/clear")
    public Result<String> clearEntrusts() {
        try {
            entrustService.remove(null);
            return Result.success("委托记录已清空");
        } catch (Exception e) {
            return Result.error("清空失败：" + e.getMessage());
        }
    }
}