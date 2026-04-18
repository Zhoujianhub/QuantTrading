package com.quant.position.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AccountInfoResponse {
    private String clientId;
    private String clientName;
    private FundInfo fund;
    private List<StockholderInfo> stockholders;

    @Data
    public static class FundInfo {
        private String fundAccount;
        private BigDecimal beginBalance;
        private BigDecimal currentBalance;
    }

    @Data
    public static class StockholderInfo {
        private String stockAccount;
        private Integer exchangeType;
        private String exchangeName;
        private Integer holderKind;
        private String openDate;
    }
}
