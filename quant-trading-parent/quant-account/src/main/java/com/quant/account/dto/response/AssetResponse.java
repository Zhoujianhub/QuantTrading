package com.quant.account.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AssetResponse {
    private String clientId;
    private BigDecimal cashAsset;
    private BigDecimal stockAsset;
    private BigDecimal totalAsset;
    private String updateTime;
    private List<PositionDetail> positions;

    @Data
    public static class PositionDetail {
        private String stockCode;
        private String stockName;
        private BigDecimal currentAmount;
        private BigDecimal currentPrice;
        private BigDecimal marketValue;
        private BigDecimal costPrice;
        private BigDecimal profitLoss;
        private BigDecimal profitLossRate;
    }
}
