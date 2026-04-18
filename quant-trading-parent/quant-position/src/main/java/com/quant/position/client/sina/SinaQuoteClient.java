package com.quant.position.client.sina;

import cn.hutool.http.HttpRequest;
import com.quant.common.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class SinaQuoteClient {

    private static final String REALTIME_QUOTE_URL = "http://hq.sinajs.cn/list={exchangeCode}{assetCode}";

    public BigDecimal getRealTimePrice(String assetCode) {
        try {
            String exchangeCode = getExchangeCode(assetCode);
            String url = REALTIME_QUOTE_URL.replace("{exchangeCode}", exchangeCode).replace("{assetCode}", assetCode);

            Map<String, String> headers = new HashMap<>();
            headers.put("Referer", "http://finance.sina.com.cn");

            String response = HttpRequest.get(url)
                    .headerMap(headers, false)
                    .timeout(5000)
                    .execute()
                    .body();

            if (response == null || response.isEmpty()) {
                log.warn("获取实时行情返回为空: assetCode={}", assetCode);
                return null;
            }

            String[] parts = response.split("\"");
            if (parts.length >= 2) {
                String[] data = parts[1].split(",");
                if (data.length >= 4) {
                    String priceStr = data[3];
                    return new BigDecimal(priceStr);
                }
            }
            return null;
        } catch (Exception e) {
            log.error("获取实时行情异常: assetCode={}", assetCode, e);
            return null;
        }
    }

    private String getExchangeCode(String assetCode) {
        if (assetCode == null || assetCode.isEmpty()) {
            return "sz";
        }
        char firstChar = assetCode.charAt(0);
        if (firstChar == '5' || firstChar == '6' || firstChar == '9' || firstChar == '7') {
            return "sh";
        }
        return "sz";
    }
}
