package com.quant.account.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("fund")
public class Fund {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String clientId;

    private String clientName;

    private String fundAccount;

    private BigDecimal beginBalance;

    private BigDecimal currentBalance;

    private String assetProp;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
