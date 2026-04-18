package com.quant.position.service;

import com.quant.position.dto.request.LoginRequest;
import com.quant.position.dto.request.RegisterRequest;
import com.quant.position.dto.request.PositionQueryRequest;
import com.quant.position.dto.response.AccountInfoResponse;
import com.quant.position.dto.response.AssetResponse;
import com.quant.position.dto.response.LoginResponse;
import com.quant.position.dto.response.PositionResponse;

import java.util.List;

public interface AccountService {

    LoginResponse login(LoginRequest request);

    void register(RegisterRequest request);

    AccountInfoResponse getAccountInfo(String clientId);

    List<PositionResponse> getPositions(PositionQueryRequest request);

    AssetResponse getAssets(String clientId);
}
