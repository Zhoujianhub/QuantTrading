package com.quant.monitor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.quant.monitor.dto.request.MonitorRuleCreateRequest;
import com.quant.monitor.dto.request.MonitorRuleUpdateRequest;
import com.quant.monitor.dto.response.MonitorRuleResponse;
import com.quant.monitor.entity.MonitorRule;
import java.util.List;

public interface MonitorRuleService {

    MonitorRuleResponse createRule(MonitorRuleCreateRequest request);

    IPage<MonitorRuleResponse> getRulePage(Integer page, Integer pageSize, String accountId);

    MonitorRuleResponse getRuleById(Long id);

    void updateRule(Long id, MonitorRuleUpdateRequest request);

    void deleteRule(Long id);

    void updateRuleStatus(Long id, Boolean enabled);

    List<MonitorRule> getEnabledRules();
}
