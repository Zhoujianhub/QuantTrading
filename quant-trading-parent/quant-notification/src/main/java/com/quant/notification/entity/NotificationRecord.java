package com.quant.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notification_record")
public class NotificationRecord {

    @TableId(type = IdType.AUTO)
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

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
