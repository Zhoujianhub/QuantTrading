package com.quant.position.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("holding")
public class Holding {

    @TableId(type = IdType.AUTO)
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

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime lastUpdatedAt;
}
