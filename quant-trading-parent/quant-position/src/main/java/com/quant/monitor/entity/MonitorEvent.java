package com.quant.monitor.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("monitor_event")
public class MonitorEvent {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ruleId;

    private String accountId;

    private String assetCode;

    private String assetName;

    private String ruleType;

    private BigDecimal triggerValue;

    private BigDecimal thresholdValue;

    private String eventStatus;

    private LocalDateTime notifiedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
