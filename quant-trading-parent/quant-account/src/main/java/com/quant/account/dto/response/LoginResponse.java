package com.quant.account.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoginResponse {
    private String clientId;
    private String clientName;
    private String fundAccount;
    private BigDecimal beginBalance;
    private BigDecimal currentBalance;
}
