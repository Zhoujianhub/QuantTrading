package com.quant.position.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quant.position.entity.EtfPriceHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface EtfPriceHistoryRepository extends BaseMapper<EtfPriceHistory> {

    @Select("SELECT * FROM etf_price_history WHERE asset_code = #{assetCode} ORDER BY price_time DESC LIMIT 1")
    EtfPriceHistory selectLatestByAssetCode(@Param("assetCode") String assetCode);

    @Select("SELECT * FROM etf_price_history WHERE asset_code = #{assetCode} AND price_time >= #{startTime}")
    List<EtfPriceHistory> selectByAssetCodeAfter(@Param("assetCode") String assetCode, @Param("startTime") LocalDateTime startTime);
}
