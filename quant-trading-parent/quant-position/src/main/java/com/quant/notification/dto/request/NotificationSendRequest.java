package com.quant.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NotificationSendRequest {

    @NotBlank(message = "通知类型不能为空")
    private String type;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String accountId;

    private String channelType = "CONSOLE";

    private String recipient;

    private Object data;

    private String channelConfig;
}
