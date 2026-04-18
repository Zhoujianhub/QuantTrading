package com.quant.notification.sender;

import com.quant.notification.dto.request.NotificationSendRequest;

public interface NotificationSender {

    String getChannelType();

    SendResult send(NotificationSendRequest request);

    record SendResult(boolean success, String message, String messageId) {}
}
