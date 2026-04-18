package com.quant.position.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class HoldingResponse {

    private Long id;
    private String accountId;
    private String assetCode;
    private String assetName;
    private String assetType;
    private String tradingType;
    private BigDecimal initialFund;
    private LocalDate openedDate;
    private BigDecimal initialNav;

    private BigDecimal costPrice;

    private BigDecimal currentNav;
    private BigDecimal currentPositionAmount;
    private BigDecimal holdingShares;
    private Integer holdingDays;
    private BigDecimal todayProfit;
    private BigDecimal todayChangeRate;
    private BigDecimal totalProfit;
    private BigDecimal totalProfitRate;
    private LocalDate currDate;
    private LocalDateTime navUpdateTime;
    private LocalDateTime importTime;
    private String sourceType;
}