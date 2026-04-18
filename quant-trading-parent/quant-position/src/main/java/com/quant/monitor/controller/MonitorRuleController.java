package com.quant.monitor.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.quant.common.result.Result;
import com.quant.monitor.dto.request.MonitorRuleCreateRequest;
import com.quant.monitor.dto.request.MonitorRuleUpdateRequest;
import com.quant.monitor.dto.response.MonitorRuleResponse;
import com.quant.monitor.service.MonitorRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/monitor/rules")
@RequiredArgsConstructor
public class MonitorRuleController {

    private final MonitorRuleService monitorRuleService;

    @PostMapping
    public Result<MonitorRuleResponse> createRule(@Valid @RequestBody MonitorRuleCreateRequest request) {
        MonitorRuleResponse response = monitorRuleService.createRule(request);
        return Result.success(response);
    }

    @GetMapping
    public Result<IPage<MonitorRuleResponse>> getRuleList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String accountId) {
        IPage<MonitorRuleResponse> result = monitorRuleService.getRulePage(page, pageSize, accountId);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<MonitorRuleResponse> getRuleById(@PathVariable Long id) {
        MonitorRuleResponse response = monitorRuleService.getRuleById(id);
        return Result.success(response);
    }

    @PutMapping("/{id}")
    public Result<Void> updateRule(@PathVariable Long id,
                                   @RequestBody MonitorRuleUpdateRequest request) {
        monitorRuleService.updateRule(id, request);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRule(@PathVariable Long id) {
        monitorRuleService.deleteRule(id);
        return Result.success(null);
    }

    @PatchMapping("/{id}/status")
    public Result<Void> updateRuleStatus(@PathVariable Long id,
                                        @RequestParam Boolean enabled) {
        monitorRuleService.updateRuleStatus(id, enabled);
        return Result.success(null);
    }
}
