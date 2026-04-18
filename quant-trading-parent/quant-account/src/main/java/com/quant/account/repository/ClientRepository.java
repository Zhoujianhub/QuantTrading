package com.quant.account.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quant.account.entity.Client;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClientRepository extends BaseMapper<Client> {
}
