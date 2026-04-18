package com.quant.position.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class HoldingAddRequest {

    @NotBlank(message = "accountId不能为空")
    private String accountId;

    @NotBlank(message = "assetCode不能为空")
    private String assetCode;

    @NotBlank(message = "assetName不能为空")
    private String assetName;

    private String assetType;
    private String tradingType;

    @NotNull(message = "initialFund不能为空")
    @Positive(message = "initialFund必须为正数")
    private BigDecimal initialFund;

    @NotNull(message = "openedDate不能为空")
    private LocalDate openedDate;
}