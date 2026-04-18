package com.quant.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notification_channel")
public class NotificationChannel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String accountId;

    private String channelName;

    private String channelType;

    private String channelConfig;

    private Boolean enabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
