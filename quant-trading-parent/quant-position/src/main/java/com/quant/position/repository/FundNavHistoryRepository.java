package com.quant.position.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quant.position.entity.FundNavHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface FundNavHistoryRepository extends BaseMapper<FundNavHistory> {

    @Select("SELECT * FROM fund_nav_history WHERE asset_code = #{assetCode} ORDER BY nav_date DESC")
    List<FundNavHistory> selectByAssetCode(@Param("assetCode") String assetCode);

    @Select("SELECT * FROM fund_nav_history WHERE asset_code = #{assetCode} AND nav_date = #{navDate}")
    FundNavHistory selectByAssetCodeAndDate(@Param("assetCode") String assetCode, @Param("navDate") LocalDate navDate);
}
