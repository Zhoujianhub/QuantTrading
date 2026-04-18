package com.quant.position.converter;

import com.quant.position.dto.request.HoldingAddRequest;
import com.quant.position.dto.response.HoldingResponse;
import com.quant.position.entity.Holding;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HoldingConverter {

    HoldingConverter INSTANCE = Mappers.getMapper(HoldingConverter.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currentNav", ignore = true)
    @Mapping(target = "currentPositionAmount", ignore = true)
    @Mapping(target = "holdingShares", ignore = true)
    @Mapping(target = "holdingDays", ignore = true)
    @Mapping(target = "todayProfit", ignore = true)
    @Mapping(target = "todayChangeRate", ignore = true)
    @Mapping(target = "totalProfit", ignore = true)
    @Mapping(target = "totalProfitRate", ignore = true)
    @Mapping(target = "currDate", ignore = true)
    @Mapping(target = "navUpdateTime", ignore = true)
    @Mapping(target = "importTime", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastUpdatedAt", ignore = true)
    Holding toEntity(HoldingAddRequest request);

    HoldingResponse toResponse(Holding entity);

    List<HoldingResponse> toResponseList(List<Holding> entities);
}
