package com.quant.monitor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MonitorRuleCreateRequest {

    @NotBlank(message = "账户ID不能为空")
    private String accountId;

    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    @NotBlank(message = "规则类型不能为空")
    private String ruleType;

    @NotNull(message = "阈值不能为空")
    private BigDecimal thresholdValue;

    @NotBlank(message = "条件类型不能为空")
    private String conditionType;

    private Boolean enabled = true;

    private Boolean notifyEnabled = true;

    private String description;
}
