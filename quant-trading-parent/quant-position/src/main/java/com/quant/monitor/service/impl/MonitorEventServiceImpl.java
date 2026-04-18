package com.quant.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quant.monitor.dto.response.MonitorEventResponse;
import com.quant.monitor.entity.MonitorEvent;
import com.quant.monitor.repository.MonitorEventRepository;
import com.quant.monitor.service.MonitorEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorEventServiceImpl implements MonitorEventService {

    private final MonitorEventRepository monitorEventRepository;

    @Override
    @Transactional
    public MonitorEvent createEvent(MonitorEvent event) {
        monitorEventRepository.insert(event);
        log.info("创建监控事件: id={}, assetCode={}, eventStatus={}",
                event.getId(), event.getAssetCode(), event.getEventStatus());
        return event;
    }

    @Override
    public IPage<MonitorEventResponse> getEventPage(Integer page, Integer pageSize,
                                                     String accountId, String eventStatus) {
        Page<MonitorEvent> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<MonitorEvent> queryWrapper = new LambdaQueryWrapper<>();

        if (accountId != null && !accountId.isEmpty()) {
            queryWrapper.eq(MonitorEvent::getAccountId, accountId);
        }
        if (eventStatus != null && !eventStatus.isEmpty()) {
            queryWrapper.eq(MonitorEvent::getEventStatus, eventStatus);
        }

        queryWrapper.orderByDesc(MonitorEvent::getId);
        IPage<MonitorEvent> result = monitorEventRepository.selectPage(pageParam, queryWrapper);

        return result.convert(this::toResponse);
    }

    @Override
    public MonitorEventResponse getEventById(Long id) {
        MonitorEvent event = monitorEventRepository.selectById(id);
        return event != null ? toResponse(event) : null;
    }

    @Override
    @Transactional
    public void updateEventStatus(MonitorEvent event) {
        monitorEventRepository.updateById(event);
        log.info("更新监控事件状态: id={}, status={}", event.getId(), event.getEventStatus());
    }

    @Override
    public boolean existsUnprocessedEvent(Long ruleId, String assetCode) {
        LambdaQueryWrapper<MonitorEvent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MonitorEvent::getRuleId, ruleId)
                    .eq(MonitorEvent::getAssetCode, assetCode)
                    .eq(MonitorEvent::getEventStatus, "TRIGGERED")
                    .gt(MonitorEvent::getCreatedAt, LocalDateTime.now().minusHours(1));
        return monitorEventRepository.selectCount(queryWrapper) > 0;
    }

    @Override
    public List<MonitorEvent> getUnprocessedEvents() {
        LambdaQueryWrapper<MonitorEvent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MonitorEvent::getEventStatus, "TRIGGERED");
        return monitorEventRepository.selectList(queryWrapper);
    }

    private MonitorEventResponse toResponse(MonitorEvent event) {
        MonitorEventResponse response = new MonitorEventResponse();
        BeanUtils.copyProperties(event, response);
        return response;
    }
}
