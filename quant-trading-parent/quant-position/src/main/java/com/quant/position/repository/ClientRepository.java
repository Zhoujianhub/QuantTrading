package com.quant.position.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quant.position.entity.Client;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClientRepository extends BaseMapper<Client> {
}
