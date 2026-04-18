package com.quant.position.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quant.position.entity.Stock;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockRepository extends BaseMapper<Stock> {
}
