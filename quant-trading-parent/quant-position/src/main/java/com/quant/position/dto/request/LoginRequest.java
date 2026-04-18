package com.quant.position.dto.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String clientId;
    private String password;
}
