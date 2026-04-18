package com.quant.monitor.service;

import com.quant.monitor.entity.MonitorEvent;
import com.quant.monitor.entity.MonitorRule;
import java.util.List;
import java.util.Map;

public interface MonitorEvaluationService {

    void evaluateAllRules();

    boolean evaluateCondition(Map<String, Object> holding, MonitorRule rule);

    MonitorEvent handleTriggeredEvent(Map<String, Object> holding, MonitorRule rule);
}
