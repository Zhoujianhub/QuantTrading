package com.quant.position.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("etf_price_history")
public class EtfPriceHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String assetCode;

    private String exchangeCode;

    private BigDecimal price;

    private BigDecimal changeRate;

    private BigDecimal changeAmount;

    private Long volume;

    private BigDecimal amount;

    private LocalDateTime priceTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
