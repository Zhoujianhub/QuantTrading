package com.quant.account.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quant.account.entity.Stockholder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockholderRepository extends BaseMapper<Stockholder> {
}
