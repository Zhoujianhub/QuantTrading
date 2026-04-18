package com.quant.notification.sender;

import com.quant.notification.dto.request.NotificationSendRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConsoleSender implements NotificationSender {

    @Override
    public String getChannelType() {
        return "CONSOLE";
    }

    @Override
    public SendResult send(NotificationSendRequest request) {
        log.info("========== 通知发送 ==========");
        log.info("类型: {}", request.getType());
        log.info("标题: {}", request.getTitle());
        log.info("内容: {}", request.getContent());
        log.info("账户: {}", request.getAccountId());
        log.info("渠道: CONSOLE");
        log.info("时间: {}", java.time.LocalDateTime.now());
        log.info("========== 通知结束 ==========");
        return new SendResult(true, "Console notification sent", null);
    }
}
