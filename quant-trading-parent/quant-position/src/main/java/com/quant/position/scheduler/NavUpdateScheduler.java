package com.quant.position.scheduler;

import com.quant.position.service.HoldingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NavUpdateScheduler {

    private final HoldingService holdingService;

    @Scheduled(cron = "0 0/30 * * * *")
    public void updateRealtimeNav() {
        log.info("开始执行盘中实时净值更新任务（每30分钟）");
        try {
            holdingService.batchUpdateNav();
            log.info("盘中实时净值更新任务执行完成");
        } catch (Exception e) {
            log.error("盘中实时净值更新任务执行失败", e);
        }
    }

    @Scheduled(cron = "0 0/5 18-23 * * *")
    public void updateHistoricalNav() {
        log.info("开始执行收盘后历史净值更新任务（18点后每5分钟）");
        try {
            holdingService.batchUpdateNavAndHistory();
            log.info("收盘后历史净值更新任务执行完成");
        } catch (Exception e) {
            log.error("收盘后历史净值更新任务执行失败", e);
        }
    }
}
