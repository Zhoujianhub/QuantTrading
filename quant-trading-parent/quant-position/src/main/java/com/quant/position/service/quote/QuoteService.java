package com.quant.position.service.quote;

import java.math.BigDecimal;

public interface QuoteService {

    BigDecimal getRealTimePrice(String assetCode);
}
