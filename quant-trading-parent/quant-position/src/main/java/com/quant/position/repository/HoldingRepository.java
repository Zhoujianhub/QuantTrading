package com.quant.position.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quant.position.entity.Holding;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface HoldingRepository extends BaseMapper<Holding> {

    @Select("SELECT * FROM holding WHERE account_id = #{accountId} AND curr_date = #{currDate}")
    List<Holding> selectByAccountIdAndDate(@Param("accountId") String accountId, @Param("currDate") LocalDate currDate);

    @Select("SELECT * FROM holding WHERE account_id = #{accountId}")
    List<Holding> selectByAccountId(@Param("accountId") String accountId);

    @Select("SELECT * FROM holding WHERE asset_code = #{assetCode}")
    List<Holding> selectByAssetCode(@Param("assetCode") String assetCode);
}
