package com.quant.monitor.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("monitor_rule")
public class MonitorRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String accountId;

    private String ruleName;

    private String ruleType;

    private BigDecimal thresholdValue;

    private String conditionType;

    private Boolean enabled;

    private Boolean notifyEnabled;

    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
