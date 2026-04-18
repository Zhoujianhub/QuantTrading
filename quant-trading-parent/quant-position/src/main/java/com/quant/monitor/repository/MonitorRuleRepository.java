package com.quant.monitor.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quant.monitor.entity.MonitorRule;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MonitorRuleRepository extends BaseMapper<MonitorRule> {
}
