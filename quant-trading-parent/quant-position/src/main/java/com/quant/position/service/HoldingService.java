package com.quant.position.service;

import com.quant.position.dto.request.HoldingAddRequest;
import com.quant.position.dto.request.HoldingQueryRequest;
import com.quant.position.dto.response.HoldingResponse;
import com.quant.position.dto.response.HoldingSummaryResponse;

import java.math.BigDecimal;
import java.util.List;

public interface HoldingService {

    HoldingResponse addHolding(HoldingAddRequest request);

    List<HoldingResponse> getHoldingsByAccountId(String accountId);

    List<HoldingResponse> getHoldingsByAccountIdAndDate(String accountId, String date);

    List<HoldingResponse> queryHoldings(HoldingQueryRequest request);

    HoldingSummaryResponse getHoldingSummary(String accountId);

    void batchUpdateNav();

    void batchUpdateNavAndHistory();

    List<HoldingResponse> getAllHoldings();

    void syncHoldingFromEntrust(String accountId, String stockCode, String stockName, Integer entrustBs, Integer entrustAmount, BigDecimal entrustPrice);
}
