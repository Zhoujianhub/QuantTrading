package com.quant.position.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("stockholder")
public class Stockholder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String clientId;

    private String clientName;

    private String fundAccount;

    private String stockAccount;

    private Integer exchangeType;

    private String openDate;

    private String cancelDate;

    private Integer holderKind;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
