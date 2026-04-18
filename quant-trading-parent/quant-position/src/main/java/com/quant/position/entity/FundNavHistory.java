package com.quant.position.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("fund_nav_history")
public class FundNavHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String assetCode;

    private LocalDate navDate;

    private BigDecimal unitNav;

    private BigDecimal accumNav;

    private BigDecimal dailyChangeRate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
