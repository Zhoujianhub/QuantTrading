package com.quant.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quant.monitor.dto.request.MonitorRuleCreateRequest;
import com.quant.monitor.dto.request.MonitorRuleUpdateRequest;
import com.quant.monitor.dto.response.MonitorRuleResponse;
import com.quant.monitor.entity.MonitorRule;
import com.quant.monitor.repository.MonitorRuleRepository;
import com.quant.monitor.service.MonitorRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorRuleServiceImpl implements MonitorRuleService {

    private final MonitorRuleRepository monitorRuleRepository;

    @Override
    @Transactional
    public MonitorRuleResponse createRule(MonitorRuleCreateRequest request) {
        MonitorRule rule = new MonitorRule();
        BeanUtils.copyProperties(request, rule);

        if (rule.getEnabled() == null) {
            rule.setEnabled(true);
        }
        if (rule.getNotifyEnabled() == null) {
            rule.setNotifyEnabled(true);
        }

        monitorRuleRepository.insert(rule);
        log.info("创建监控规则成功: id={}, ruleName={}", rule.getId(), rule.getRuleName());

        return toResponse(rule);
    }

    @Override
    public IPage<MonitorRuleResponse> getRulePage(Integer page, Integer pageSize, String accountId) {
        Page<MonitorRule> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<MonitorRule> queryWrapper = new LambdaQueryWrapper<>();

        if (accountId != null && !accountId.isEmpty()) {
            queryWrapper.eq(MonitorRule::getAccountId, accountId);
        }

        queryWrapper.orderByDesc(MonitorRule::getId);
        IPage<MonitorRule> result = monitorRuleRepository.selectPage(pageParam, queryWrapper);

        return result.convert(this::toResponse);
    }

    @Override
    public MonitorRuleResponse getRuleById(Long id) {
        MonitorRule rule = monitorRuleRepository.selectById(id);
        return rule != null ? toResponse(rule) : null;
    }

    @Override
    @Transactional
    public void updateRule(Long id, MonitorRuleUpdateRequest request) {
        MonitorRule rule = monitorRuleRepository.selectById(id);
        if (rule == null) {
            throw new RuntimeException("监控规则不存在: " + id);
        }

        if (request.getRuleName() != null) {
            rule.setRuleName(request.getRuleName());
        }
        if (request.getRuleType() != null) {
            rule.setRuleType(request.getRuleType());
        }
        if (request.getThresholdValue() != null) {
            rule.setThresholdValue(request.getThresholdValue());
        }
        if (request.getConditionType() != null) {
            rule.setConditionType(request.getConditionType());
        }
        if (request.getEnabled() != null) {
            rule.setEnabled(request.getEnabled());
        }
        if (request.getNotifyEnabled() != null) {
            rule.setNotifyEnabled(request.getNotifyEnabled());
        }
        if (request.getDescription() != null) {
            rule.setDescription(request.getDescription());
        }

        monitorRuleRepository.updateById(rule);
        log.info("更新监控规则成功: id={}", id);
    }

    @Override
    @Transactional
    public void deleteRule(Long id) {
        monitorRuleRepository.deleteById(id);
        log.info("删除监控规则成功: id={}", id);
    }

    @Override
    @Transactional
    public void updateRuleStatus(Long id, Boolean enabled) {
        MonitorRule rule = monitorRuleRepository.selectById(id);
        if (rule == null) {
            throw new RuntimeException("监控规则不存在: " + id);
        }

        rule.setEnabled(enabled);
        monitorRuleRepository.updateById(rule);
        log.info("更新监控规则状态: id={}, enabled={}", id, enabled);
    }

    @Override
    public List<MonitorRule> getEnabledRules() {
        LambdaQueryWrapper<MonitorRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MonitorRule::getEnabled, true);
        return monitorRuleRepository.selectList(queryWrapper);
    }

    private MonitorRuleResponse toResponse(MonitorRule rule) {
        MonitorRuleResponse response = new MonitorRuleResponse();
        BeanUtils.copyProperties(rule, response);
        return response;
    }
}
