package com.quant.account.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quant.account.entity.Stock;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockRepository extends BaseMapper<Stock> {
}
