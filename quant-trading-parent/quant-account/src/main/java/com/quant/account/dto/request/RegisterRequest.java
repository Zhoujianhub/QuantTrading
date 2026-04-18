package com.quant.account.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RegisterRequest {
    private String clientName;
    private String password;
    private BigDecimal beginBalance;
}
