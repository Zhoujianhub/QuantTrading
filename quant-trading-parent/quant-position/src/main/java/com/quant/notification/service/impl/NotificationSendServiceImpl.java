package com.quant.notification.service.impl;

import com.quant.notification.dto.request.NotificationSendRequest;
import com.quant.notification.entity.NotificationRecord;
import com.quant.notification.sender.NotificationSender;
import com.quant.notification.service.NotificationSendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Service
public class NotificationSendServiceImpl implements NotificationSendService {

    private final Map<String, NotificationSender> senderMap;
    private final List<NotificationSender> senders;

    public NotificationSendServiceImpl(List<NotificationSender> senderList) {
        this.senders = senderList;
        this.senderMap = new HashMap<>();
        for (NotificationSender sender : senderList) {
            senderMap.put(sender.getChannelType(), sender);
        }
        log.info("已注册的发送器: {}", senderMap.keySet());
    }

    @Override
    @Transactional
    public NotificationRecord send(NotificationSendRequest request) {
        log.info("开始发送通知: type={}, channelType={}, title={}",
                request.getType(), request.getChannelType(), request.getTitle());

        NotificationRecord record = new NotificationRecord();
        record.setAccountId(request.getAccountId());
        record.setNotificationType(request.getType());
        record.setChannelType(request.getChannelType());
        record.setTitle(request.getTitle());
        record.setContent(request.getContent());
        record.setRecipient(request.getRecipient());
        record.setStatus("PENDING");
        record.setCreatedAt(LocalDateTime.now());

        try {
            NotificationSender sender = senderMap.get(request.getChannelType());
            if (sender == null) {
                log.warn("未找到对应渠道的发送器，使用默认CONSOLE: {}", request.getChannelType());
                sender = senderMap.get("CONSOLE");
            }

            if (sender != null) {
                NotificationSender.SendResult result = sender.send(request);

                if (result.success()) {
                    record.setStatus("SUCCESS");
                    record.setSendTime(LocalDateTime.now());
                    log.info("通知发送成功: type={}, title={}", request.getType(), request.getTitle());
                } else {
                    record.setStatus("FAILED");
                    record.setErrorMessage(result.message());
                    log.warn("通知发送失败: type={}, error={}", request.getType(), result.message());
                }
            } else {
                record.setStatus("FAILED");
                record.setErrorMessage("No sender available for channel: " + request.getChannelType());
            }
        } catch (Exception e) {
            record.setStatus("FAILED");
            record.setErrorMessage(e.getMessage());
            log.error("通知发送异常: type={}", request.getType(), e);
        }

        return record;
    }
}
