package com.quant.position.service.quote;

import com.quant.position.client.sina.SinaQuoteClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteServiceImpl implements QuoteService {

    private final SinaQuoteClient sinaQuoteClient;

    @Override
    public BigDecimal getRealTimePrice(String assetCode) {
        try {
            return sinaQuoteClient.getRealTimePrice(assetCode);
        } catch (Exception e) {
            log.error("获取实时行情失败: assetCode={}", assetCode, e);
            return null;
        }
    }
}
