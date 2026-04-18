package com.quant.notification.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationRecordResponse {

    private Long id;

    private String accountId;

    private String notificationType;

    private String channelType;

    private String title;

    private String content;

    private String recipient;

    private String status;

    private String errorMessage;

    private LocalDateTime sendTime;

    private LocalDateTime createdAt;
}
