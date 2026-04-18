package com.quant.account.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("stock")
public class Stock {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String clientId;

    private String fundAccount;

    private Integer exchangeType;

    private String stockAccount;

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

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
