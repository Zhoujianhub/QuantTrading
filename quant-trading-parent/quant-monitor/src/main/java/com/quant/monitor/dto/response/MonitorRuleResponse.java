package com.quant.monitor.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MonitorRuleResponse {

    private Long id;

    private String accountId;

    private String ruleName;

    private String ruleType;

    private BigDecimal thresholdValue;

    private String conditionType;

    private Boolean enabled;

    private Boolean notifyEnabled;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
