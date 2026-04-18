package com.quant.monitor.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MonitorRuleUpdateRequest {

    private String ruleName;

    private String ruleType;

    private BigDecimal thresholdValue;

    private String conditionType;

    private Boolean enabled;

    private Boolean notifyEnabled;

    private String description;
}
