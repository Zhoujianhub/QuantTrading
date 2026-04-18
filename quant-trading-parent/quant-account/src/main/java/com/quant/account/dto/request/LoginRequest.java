package com.quant.account.dto.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String clientId;
    private String password;
}
