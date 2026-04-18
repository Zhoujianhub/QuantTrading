package com.quant.monitor.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.quant.common.result.Result;
import com.quant.monitor.dto.response.MonitorEventResponse;
import com.quant.monitor.entity.MonitorEvent;
import com.quant.monitor.service.MonitorEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/monitor/events")
@RequiredArgsConstructor
public class MonitorEventController {

    private final MonitorEventService monitorEventService;

    @GetMapping
    public Result<IPage<MonitorEventResponse>> getEventList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) String eventStatus) {
        IPage<MonitorEventResponse> result = monitorEventService.getEventPage(page, pageSize, accountId, eventStatus);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<MonitorEventResponse> getEventById(@PathVariable Long id) {
        MonitorEventResponse response = monitorEventService.getEventById(id);
        return Result.success(response);
    }

    @PatchMapping("/{id}/status")
    public Result<Void> updateEventStatus(@PathVariable Long id,
                                          @RequestParam String status) {
        MonitorEventResponse response = monitorEventService.getEventById(id);
        if (response != null) {
            MonitorEvent event = new MonitorEvent();
            event.setId(response.getId());
            event.setEventStatus(status);
            monitorEventService.updateEventStatus(event);
        }
        return Result.success(null);
    }
}
