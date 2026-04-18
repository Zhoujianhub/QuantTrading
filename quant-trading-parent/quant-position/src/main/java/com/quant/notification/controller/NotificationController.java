package com.quant.notification.controller;

import com.quant.common.result.Result;
import com.quant.notification.dto.request.NotificationSendRequest;
import com.quant.notification.entity.NotificationRecord;
import com.quant.notification.service.NotificationSendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationSendService notificationSendService;

    @PostMapping("/send")
    public Result<NotificationRecord> sendNotification(@Valid @RequestBody NotificationSendRequest request) {
        NotificationRecord record = notificationSendService.send(request);
        return Result.success(record);
    }
}
