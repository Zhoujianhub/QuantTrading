package com.quant.notification.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quant.notification.entity.NotificationChannel;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotificationChannelRepository extends BaseMapper<NotificationChannel> {
}
