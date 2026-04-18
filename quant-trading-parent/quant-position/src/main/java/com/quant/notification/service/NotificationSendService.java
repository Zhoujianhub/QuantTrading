package com.quant.notification.service;

import com.quant.notification.dto.request.NotificationSendRequest;
import com.quant.notification.entity.NotificationRecord;

public interface NotificationSendService {

    NotificationRecord send(NotificationSendRequest request);
}
