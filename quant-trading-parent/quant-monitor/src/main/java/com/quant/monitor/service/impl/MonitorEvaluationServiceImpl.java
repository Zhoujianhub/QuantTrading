package com.quant.monitor.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quant.monitor.client.NotificationClient;
import com.quant.monitor.entity.MonitorEvent;
import com.quant.monitor.entity.MonitorRule;
import com.quant.monitor.service.MonitorEventService;
import com.quant.monitor.service.MonitorEvaluationService;
import com.quant.monitor.service.MonitorRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorEvaluationServiceImpl implements MonitorEvaluationService {

    private final MonitorRuleService monitorRuleService;
    private final MonitorEventService monitorEventService;
    private final NotificationClient notificationClient;

    @Value("${position.api.base-url:http://localhost:8080}")
    private String positionApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @SuppressWarnings("unchecked")
    public void evaluateAllRules() {
        log.info("开始执行监控规则评估任务");

        List<MonitorRule> enabledRules = monitorRuleService.getEnabledRules();
        if (enabledRules.isEmpty()) {
            log.info("没有启用的监控规则，跳过评估");
            return;
        }

        List<Map<String, Object>> holdings = fetchHoldings();
        if (holdings.isEmpty()) {
            log.info("没有持仓数据，跳过评估");
            return;
        }

        log.info("待评估规则数: {}, 持仓数: {}", enabledRules.size(), holdings.size());

        int triggeredCount = 0;
        for (MonitorRule rule : enabledRules) {
            for (Map<String, Object> holding : holdings) {
                try {
                    if (evaluateCondition(holding, rule)) {
                        triggeredCount++;
                        handleTriggeredEvent(holding, rule);
                    }
                } catch (Exception e) {
                    log.error("评估规则异常: ruleId={}, assetCode={}",
                            rule.getId(), holding.get("assetCode"), e);
                }
            }
        }

        log.info("监控规则评估任务完成，触发事件数: {}", triggeredCount);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchHoldings() {
        try {
            String url = positionApiUrl + "/api/v1/holdings/list";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.get("code") != null && Integer.valueOf(response.get("code").toString()) == 200) {
                Object data = response.get("data");
                if (data instanceof List) {
                    return (List<Map<String, Object>>) data;
                }
            }
        } catch (Exception e) {
            log.error("获取持仓数据失败: {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    public boolean evaluateCondition(Map<String, Object> holding, MonitorRule rule) {
        BigDecimal currentValue = getEvaluationValue(holding, rule.getRuleType());
        if (currentValue == null) {
            return false;
        }

        BigDecimal threshold = rule.getThresholdValue();
        String condition = rule.getConditionType();

        boolean result = switch (condition) {
            case "GT" -> currentValue.compareTo(threshold) > 0;
            case "GTE" -> currentValue.compareTo(threshold) >= 0;
            case "LT" -> currentValue.compareTo(threshold) < 0;
            case "LTE" -> currentValue.compareTo(threshold) <= 0;
            case "EQ" -> currentValue.compareTo(threshold) == 0;
            default -> false;
        };

        log.debug("评估条件: assetCode={}, ruleType={}, currentValue={}, condition={}, threshold={}, result={}",
                holding.get("assetCode"), rule.getRuleType(), currentValue, condition, threshold, result);

        return result;
    }

    private BigDecimal getEvaluationValue(Map<String, Object> holding, String ruleType) {
        try {
            if ("PROFIT_RATE".equals(ruleType)) {
                Object profitRate = holding.get("totalProfitRate");
                if (profitRate != null) {
                    return new BigDecimal(profitRate.toString());
                }
            } else if ("TODAY_CHANGE_RATE".equals(ruleType)) {
                Object changeRate = holding.get("todayChangeRate");
                if (changeRate != null) {
                    return new BigDecimal(changeRate.toString());
                }
            }
        } catch (Exception e) {
            log.warn("获取评估值失败: ruleType={}", ruleType, e);
        }
        return null;
    }

    @Override
    public MonitorEvent handleTriggeredEvent(Map<String, Object> holding, MonitorRule rule) {
        String assetCode = (String) holding.get("assetCode");

        if (monitorEventService.existsUnprocessedEvent(rule.getId(), assetCode)) {
            log.debug("事件已存在，跳过: ruleId={}, assetCode={}", rule.getId(), assetCode);
            return null;
        }

        MonitorEvent event = new MonitorEvent();
        event.setRuleId(rule.getId());
        event.setAccountId((String) holding.get("accountId"));
        event.setAssetCode(assetCode);
        event.setAssetName((String) holding.get("assetName"));
        event.setRuleType(rule.getRuleType());
        event.setTriggerValue(getEvaluationValue(holding, rule.getRuleType()));
        event.setThresholdValue(rule.getThresholdValue());
        event.setEventStatus("TRIGGERED");
        event.setCreatedAt(LocalDateTime.now());

        monitorEventService.createEvent(event);

        if (rule.getNotifyEnabled()) {
            try {
                notificationClient.sendAlertNotification(event);
                event.setEventStatus("NOTIFIED");
                event.setNotifiedAt(LocalDateTime.now());
                monitorEventService.updateEventStatus(event);
                log.info("监控告警已发送: ruleName={}, assetCode={}, triggerValue={}",
                        rule.getRuleName(), assetCode, event.getTriggerValue());
            } catch (Exception e) {
                log.error("发送通知失败: eventId={}", event.getId(), e);
            }
        }

        return event;
    }
}
