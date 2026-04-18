package com.quant.position.dto.request;

import lombok.Data;

@Data
public class HoldingQueryRequest {

    private String accountId;

    private String assetType;

    private String keyword;

    private Integer page = 1;

    private Integer pageSize = 20;
}
