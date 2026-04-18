package com.quant.account.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quant.account.entity.Fund;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FundRepository extends BaseMapper<Fund> {
}
