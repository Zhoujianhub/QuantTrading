package com.quant.monitor.client;

import com.quant.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class NotificationClient {

    @Value("${notification.api.base-url:http://localhost:8080}")
    private String baseUrl;

    public void sendAlertNotification(Object eventObj) {
        try {
            String url = baseUrl + "/api/v1/notifications/send";

            Map<String, Object> request = new HashMap<>();
            request.put("type", "MONITOR_ALERT");
            request.put("title", "持仓监控告警");
            request.put("content", "监控规则触发，请查看详情");
            request.put("data", eventObj);

            log.info("调用通知模块发送告警: url={}, request={}", url, request);

        } catch (Exception e) {
            log.error("调用通知模块失败", e);
            throw e;
        }
    }
}
