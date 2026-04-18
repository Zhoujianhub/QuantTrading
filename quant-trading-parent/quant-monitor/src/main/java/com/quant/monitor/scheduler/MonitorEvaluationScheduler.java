package com.quant.monitor.scheduler;

import com.quant.monitor.service.MonitorEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonitorEvaluationScheduler {

    private final MonitorEvaluationService monitorEvaluationService;

    @Scheduled(cron = "0 */5 * * * *")
    public void evaluateRules() {
        log.info("定时任务触发：监控规则评估");
        try {
            monitorEvaluationService.evaluateAllRules();
        } catch (Exception e) {
            log.error("监控规则评估任务执行失败", e);
        }
    }
}
