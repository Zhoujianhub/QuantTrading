package com.quant.account.dto.request;

import lombok.Data;

@Data
public class PositionQueryRequest {
    private String clientId;
    private String fundAccount;
    private String stockAccount;
    private Integer exchangeType;
}
