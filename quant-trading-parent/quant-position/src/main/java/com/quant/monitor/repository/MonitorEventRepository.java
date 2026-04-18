package com.quant.monitor.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quant.monitor.entity.MonitorEvent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MonitorEventRepository extends BaseMapper<MonitorEvent> {
}
