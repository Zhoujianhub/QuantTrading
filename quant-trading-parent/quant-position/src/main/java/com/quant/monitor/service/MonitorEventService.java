package com.quant.monitor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.quant.monitor.dto.response.MonitorEventResponse;
import com.quant.monitor.entity.MonitorEvent;
import java.util.List;

public interface MonitorEventService {

    MonitorEvent createEvent(MonitorEvent event);

    IPage<MonitorEventResponse> getEventPage(Integer page, Integer pageSize, String accountId, String eventStatus);

    MonitorEventResponse getEventById(Long id);

    void updateEventStatus(MonitorEvent event);

    boolean existsUnprocessedEvent(Long ruleId, String assetCode);

    List<MonitorEvent> getUnprocessedEvents();
}
