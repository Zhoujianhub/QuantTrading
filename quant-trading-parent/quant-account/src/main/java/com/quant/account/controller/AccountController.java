package com.quant.account.controller;

import com.quant.account.dto.request.LoginRequest;
import com.quant.account.dto.request.RegisterRequest;
import com.quant.account.dto.request.PositionQueryRequest;
import com.quant.account.dto.response.AccountInfoResponse;
import com.quant.account.dto.response.AssetResponse;
import com.quant.account.dto.response.LoginResponse;
import com.quant.account.dto.response.PositionResponse;
import com.quant.account.service.AccountService;
import com.quant.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        return Result.success(accountService.login(request));
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterRequest request) {
        accountService.register(request);
        return Result.success(null);
    }

    @GetMapping("/info/{clientId}")
    public Result<AccountInfoResponse> getAccountInfo(@PathVariable String clientId) {
        return Result.success(accountService.getAccountInfo(clientId));
    }

    @GetMapping("/positions")
    public Result<List<PositionResponse>> getPositions(PositionQueryRequest request) {
        return Result.success(accountService.getPositions(request));
    }

    @GetMapping("/assets/{clientId}")
    public Result<AssetResponse> getAssets(@PathVariable String clientId) {
        return Result.success(accountService.getAssets(clientId));
    }
}
