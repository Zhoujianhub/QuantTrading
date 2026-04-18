package com.quant.position.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HoldingSummaryResponse {

    private String accountId;
    private Integer totalAssets;
    private BigDecimal totalInitialFund;
    private BigDecimal totalCurrentValue;
    private BigDecimal totalProfit;
    private BigDecimal totalProfitRate;
    private BigDecimal todayTotalProfit;
    private BigDecimal todayTotalChangeRate;
}