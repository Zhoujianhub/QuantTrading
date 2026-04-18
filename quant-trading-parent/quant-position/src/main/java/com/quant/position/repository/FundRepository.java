package com.quant.position.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quant.position.entity.Fund;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FundRepository extends BaseMapper<Fund> {
}
