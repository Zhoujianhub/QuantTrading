package com.quant.position.service.fund;

import java.math.BigDecimal;

public interface FundNavService {

    BigDecimal getRealTimeNav(String assetCode);
}
