package com.quant.position.service.fund;

import com.quant.position.client.eastmoney.EastMoneyClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundNavServiceImpl implements FundNavService {

    private final EastMoneyClient eastMoneyClient;

    @Override
    public BigDecimal getRealTimeNav(String assetCode) {
        try {
            return eastMoneyClient.getRealTimeNav(assetCode);
        } catch (Exception e) {
            log.error("获取基金实时净值失败: assetCode={}", assetCode, e);
            return null;
        }
    }
}
