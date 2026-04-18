package com.quant.position.controller;

import com.quant.common.result.Result;
import com.quant.position.dto.request.HoldingAddRequest;
import com.quant.position.dto.request.HoldingQueryRequest;
import com.quant.position.dto.response.HoldingResponse;
import com.quant.position.dto.response.HoldingSummaryResponse;
import com.quant.position.service.HoldingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/holdings")
@RequiredArgsConstructor
public class HoldingController {

    private final HoldingService holdingService;

    @PostMapping
    public Result<HoldingResponse> addHolding(@RequestBody HoldingAddRequest request) {
        return Result.success(holdingService.addHolding(request));
    }

    @GetMapping("/account/{accountId}")
    public Result<List<HoldingResponse>> getHoldingsByAccountId(@PathVariable String accountId) {
        return Result.success(holdingService.getHoldingsByAccountId(accountId));
    }

    @GetMapping("/client/{clientId}")
    public Result<List<HoldingResponse>> getHoldingsByClientId(@PathVariable String clientId) {
        return Result.success(holdingService.getHoldingsByAccountId(clientId));
    }

    @GetMapping("/account/{accountId}/date/{date}")
    public Result<List<HoldingResponse>> getHoldingsByAccountIdAndDate(
            @PathVariable String accountId, @PathVariable String date) {
        return Result.success(holdingService.getHoldingsByAccountIdAndDate(accountId, date));
    }

    @GetMapping("/query")
    public Result<List<HoldingResponse>> queryHoldings(HoldingQueryRequest request) {
        return Result.success(holdingService.queryHoldings(request));
    }

    @GetMapping("/account/{accountId}/summary")
    public Result<HoldingSummaryResponse> getHoldingSummary(@PathVariable String accountId) {
        return Result.success(holdingService.getHoldingSummary(accountId));
    }

    @PostMapping("/test-batch-update")
    public Result<String> testBatchUpdate() {
        holdingService.batchUpdateNav();
        return Result.success("Batch update triggered");
    }

    @GetMapping("/test-query")
    public Result<List<HoldingResponse>> testQuery() {
        return Result.success(holdingService.getHoldingsByAccountId("TEST001"));
    }

    @PostMapping("/sync-from-entrust")
    public Result<String> syncFromEntrust(@RequestParam String accountId,
                                          @RequestParam String stockCode,
                                          @RequestParam String stockName,
                                          @RequestParam Integer entrustBs,
                                          @RequestParam Integer entrustAmount,
                                          @RequestParam BigDecimal entrustPrice) {
        holdingService.syncHoldingFromEntrust(accountId, stockCode, stockName, entrustBs, entrustAmount, entrustPrice);
        return Result.success("持仓同步成功");
    }
}