package com.quant.account.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("client")
public class Client {

    @TableId(type = IdType.INPUT)
    private String clientId;

    private String clientName;

    private String openDate;

    private String cancelDate;

    private String password;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
