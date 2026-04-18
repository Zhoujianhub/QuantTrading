package com.quant.account.service;

import com.quant.account.dto.request.LoginRequest;
import com.quant.account.dto.request.RegisterRequest;
import com.quant.account.dto.request.PositionQueryRequest;
import com.quant.account.dto.response.AccountInfoResponse;
import com.quant.account.dto.response.AssetResponse;
import com.quant.account.dto.response.LoginResponse;
import com.quant.account.dto.response.PositionResponse;

import java.util.List;

public interface AccountService {

    LoginResponse login(LoginRequest request);

    void register(RegisterRequest request);

    AccountInfoResponse getAccountInfo(String clientId);

    List<PositionResponse> getPositions(PositionQueryRequest request);

    AssetResponse getAssets(String clientId);
}
