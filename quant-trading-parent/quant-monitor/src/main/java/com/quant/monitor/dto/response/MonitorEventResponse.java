package com.quant.monitor.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MonitorEventResponse {

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

    private LocalDateTime createdAt;
}
