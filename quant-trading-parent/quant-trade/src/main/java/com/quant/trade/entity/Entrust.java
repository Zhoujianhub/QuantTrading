package com.quant.trade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("entrust")
public class Entrust {

    private LocalDate initDate;

    private LocalDate currDate;

    @TableId(type = IdType.INPUT)
    private String entrustNo;

    private String clientId;

    private String fundAccount;

    private Integer exchangeType;

    private String stockAccount;

    private String stockCode;

    private String stockName;

    private Integer stockType;

    private Integer entrustBs;

    private String direction;

    private BigDecimal entrustAmount;

    private BigDecimal entrustPrice;

    private BigDecimal businessAmount;

    private BigDecimal withdrawAmount;

    private BigDecimal businessPrice;

    private Integer entrustStatus;

    private String entrustProp;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}