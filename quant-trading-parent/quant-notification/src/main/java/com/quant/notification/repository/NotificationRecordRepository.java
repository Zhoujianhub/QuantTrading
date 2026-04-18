package com.quant.notification.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quant.notification.entity.NotificationRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotificationRecordRepository extends BaseMapper<NotificationRecord> {
}
