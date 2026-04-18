package com.quant.position.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PositionResponse {
    private String fundAccount;
    private String stockAccount;
    private Integer exchangeType;
    private String stockCode;
    private String stockName;
    private String stockType;
    private BigDecimal beginAmount;
    private BigDecimal currentAmount;
    private BigDecimal sumBuyAmount;
    private BigDecimal sumBuyBalance;
    private BigDecimal sumSellAmount;
    private BigDecimal sumSellBalance;
    private BigDecimal costPrice;
}
