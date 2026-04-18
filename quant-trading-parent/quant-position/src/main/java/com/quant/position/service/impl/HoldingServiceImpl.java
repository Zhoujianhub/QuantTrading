package com.quant.position.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.quant.common.exception.BusinessException;
import com.quant.common.result.ResultCode;
import com.quant.position.converter.HoldingConverter;
import com.quant.position.dto.request.HoldingAddRequest;
import com.quant.position.dto.request.HoldingQueryRequest;
import com.quant.position.dto.response.HoldingResponse;
import com.quant.position.dto.response.HoldingSummaryResponse;
import com.quant.position.entity.Holding;
import com.quant.position.repository.HoldingRepository;
import com.quant.position.service.fund.FundNavService;
import com.quant.position.service.quote.QuoteService;
import com.quant.position.util.NavCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HoldingServiceImpl implements com.quant.position.service.HoldingService {

    private final HoldingRepository holdingRepository;
    private final HoldingConverter holdingConverter;
    private final FundNavService fundNavService;
    private final QuoteService quoteService;

    @Override
    @Transactional
    public HoldingResponse addHolding(HoldingAddRequest request) {
        Holding holding = holdingConverter.toEntity(request);
        holding.setCurrDate(LocalDate.now());
        holding.setImportTime(LocalDateTime.now());
        holding.setHoldingDays(0);
        holding.setTodayProfit(BigDecimal.ZERO);
        holding.setTodayChangeRate(BigDecimal.ZERO);
        holding.setTotalProfit(BigDecimal.ZERO);
        holding.setTotalProfitRate(BigDecimal.ZERO);
        holding.setCurrentPositionAmount(request.getInitialFund());

        BigDecimal currentNav = fetchCurrentNav(request.getAssetCode(), request.getTradingType());
        holding.setCurrentNav(currentNav);
        holding.setNavUpdateTime(LocalDateTime.now());

        if (currentNav != null && currentNav.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal shares = request.getInitialFund().divide(currentNav, 4, java.math.RoundingMode.HALF_UP);
            holding.setHoldingShares(shares);
            holding.setInitialNav(currentNav);
            holding.setCostPrice(currentNav);

            BigDecimal totalProfit = currentNav.subtract(request.getInitialFund());
            holding.setTotalProfit(totalProfit);
            if (request.getInitialFund().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal profitRate = totalProfit.divide(request.getInitialFund(), 4, java.math.RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                holding.setTotalProfitRate(profitRate);
            }
        }

        holdingRepository.insert(holding);
        log.info("持仓记录添加成功: assetCode={}, accountId={}", request.getAssetCode(), request.getAccountId());
        return holdingConverter.toResponse(holding);
    }

    @Override
    public List<HoldingResponse> getHoldingsByAccountId(String accountId) {
        List<Holding> holdings = holdingRepository.selectByAccountId(accountId);
        return holdingConverter.toResponseList(holdings);
    }

    @Override
    public List<HoldingResponse> getHoldingsByAccountIdAndDate(String accountId, String date) {
        LocalDate currentDate = LocalDate.parse(date);
        List<Holding> holdings = holdingRepository.selectByAccountIdAndDate(accountId, currentDate);
        return holdingConverter.toResponseList(holdings);
    }

    @Override
    public List<HoldingResponse> queryHoldings(HoldingQueryRequest request) {
        LambdaQueryWrapper<Holding> queryWrapper = new LambdaQueryWrapper<>();

        if (request.getAccountId() != null && !request.getAccountId().isEmpty()) {
            queryWrapper.eq(Holding::getAccountId, request.getAccountId());
        }

        if (request.getAssetType() != null && !request.getAssetType().isEmpty()) {
            queryWrapper.eq(Holding::getAssetType, request.getAssetType());
        }

        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Holding::getAssetCode, request.getKeyword())
                    .or()
                    .like(Holding::getAssetName, request.getKeyword())
            );
        }

        queryWrapper.orderByDesc(Holding::getId);

        List<Holding> holdings = holdingRepository.selectList(queryWrapper);
        return holdingConverter.toResponseList(holdings);
    }

    @Override
    public HoldingSummaryResponse getHoldingSummary(String accountId) {
        List<Holding> holdings = holdingRepository.selectByAccountId(accountId);

        HoldingSummaryResponse summary = new HoldingSummaryResponse();
        summary.setAccountId(accountId);
        summary.setTotalAssets(holdings.size());

        BigDecimal totalInitialFund = holdings.stream()
                .map(Holding::getInitialFund)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTotalInitialFund(totalInitialFund);

        BigDecimal totalCurrentValue = holdings.stream()
                .map(Holding::getCurrentPositionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTotalCurrentValue(totalCurrentValue);

        BigDecimal totalProfit = totalCurrentValue.subtract(totalInitialFund);
        summary.setTotalProfit(totalProfit);

        if (totalInitialFund.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal profitRate = totalProfit.divide(totalInitialFund, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            summary.setTotalProfitRate(profitRate);
        } else {
            summary.setTotalProfitRate(BigDecimal.ZERO);
        }

        BigDecimal todayTotalProfit = holdings.stream()
                .map(Holding::getTodayProfit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTodayTotalProfit(todayTotalProfit);

        return summary;
    }

    @Override
    public void batchUpdateNav() {
        List<Holding> holdings = holdingRepository.selectList(
                Wrappers.<Holding>lambdaQuery()
                        .eq(Holding::getCurrDate, LocalDate.now())
                        .orderByAsc(Holding::getId)
        );

        log.info("开始批量更新净值，待更新记录数: {}", holdings.size());

        for (int i = 0; i < holdings.size(); i++) {
            Holding holding = holdings.get(i);
            log.info("开始处理第{}条记录: assetCode={}, tradingType={}", i + 1, holding.getAssetCode(), holding.getTradingType());
            try {
                BigDecimal currentNav = fetchCurrentNav(holding.getAssetCode(), holding.getTradingType());
                log.info("获取到净值: assetCode={}, currentNav={}", holding.getAssetCode(), currentNav);
                if (currentNav != null) {
                    BigDecimal yesterdayNav = holding.getCurrentNav();
                    holding.setCurrentNav(currentNav);
                    holding.setNavUpdateTime(LocalDateTime.now());

                    NavCalculator.calculateProfit(holding, yesterdayNav);

                    holdingRepository.updateById(holding);
                    log.info("净值更新成功: assetCode={}, currentNav={}", holding.getAssetCode(), currentNav);
                } else {
                    log.warn("获取净值为空: assetCode={}", holding.getAssetCode());
                }
            } catch (Exception e) {
                log.error("净值更新失败: assetCode={}", holding.getAssetCode(), e);
            }
        }
    }

    private BigDecimal fetchCurrentNav(String assetCode, String tradingType) {
        if ("场内".equals(tradingType)) {
            return quoteService.getRealTimePrice(assetCode);
        } else {
            return fundNavService.getRealTimeNav(assetCode);
        }
    }

    @Override
    public void batchUpdateNavAndHistory() {
        log.info("开始执行收盘后历史净值更新任务");
        batchUpdateNav();
        log.info("收盘后历史净值更新任务完成");
    }

    @Override
    public List<HoldingResponse> getAllHoldings() {
        List<Holding> holdings = holdingRepository.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Holding>()
                        .orderByDesc(Holding::getId)
        );
        return holdingConverter.toResponseList(holdings);
    }

    @Override
    @Transactional
    public void syncHoldingFromEntrust(String accountId, String stockCode, String stockName, Integer entrustBs, Integer entrustAmount, BigDecimal entrustPrice) {
        LambdaQueryWrapper<Holding> queryWrapper = new LambdaQueryWrapper<Holding>()
                .eq(Holding::getAccountId, accountId)
                .eq(Holding::getAssetCode, stockCode);
        Holding existingHolding = holdingRepository.selectOne(queryWrapper);

        if (entrustBs == 1) {
            if (existingHolding != null) {
                BigDecimal totalShares = existingHolding.getHoldingShares().add(new BigDecimal(entrustAmount));
                BigDecimal totalCost = existingHolding.getCostPrice().multiply(existingHolding.getHoldingShares())
                        .add(entrustPrice.multiply(new BigDecimal(entrustAmount)));
                BigDecimal newCostPrice = totalCost.divide(totalShares, 4, BigDecimal.ROUND_HALF_UP);
                
                existingHolding.setHoldingShares(totalShares);
                existingHolding.setCostPrice(newCostPrice);
                existingHolding.setCurrentPositionAmount(totalShares.multiply(existingHolding.getCurrentNav()));
                holdingRepository.updateById(existingHolding);
                log.info("持仓更新成功(买入): accountId={}, stockCode={}, 新增数量={}, 新成本价={}", 
                        accountId, stockCode, entrustAmount, newCostPrice);
            } else {
                Holding newHolding = new Holding();
                newHolding.setAccountId(accountId);
                newHolding.setAssetCode(stockCode);
                newHolding.setAssetName(stockName);
                newHolding.setAssetType("股票");
                newHolding.setTradingType("普通");
                newHolding.setHoldingShares(new BigDecimal(entrustAmount));
                newHolding.setCostPrice(entrustPrice);
                newHolding.setCurrentNav(entrustPrice);
                newHolding.setCurrentPositionAmount(entrustPrice.multiply(new BigDecimal(entrustAmount)));
                newHolding.setInitialFund(entrustPrice.multiply(new BigDecimal(entrustAmount)));
                newHolding.setInitialNav(entrustPrice);
                newHolding.setOpenedDate(LocalDate.now());
                newHolding.setCurrDate(LocalDate.now());
                newHolding.setImportTime(LocalDateTime.now());
                newHolding.setHoldingDays(0);
                newHolding.setTodayProfit(BigDecimal.ZERO);
                newHolding.setTodayChangeRate(BigDecimal.ZERO);
                newHolding.setTotalProfit(BigDecimal.ZERO);
                newHolding.setTotalProfitRate(BigDecimal.ZERO);
                holdingRepository.insert(newHolding);
                log.info("持仓新增成功(买入): accountId={}, stockCode={}, 数量={}", accountId, stockCode, entrustAmount);
            }
        } else if (entrustBs == 2) {
            if (existingHolding != null) {
                BigDecimal currentShares = existingHolding.getHoldingShares();
                if (currentShares.compareTo(new BigDecimal(entrustAmount)) >= 0) {
                    BigDecimal remainingShares = currentShares.subtract(new BigDecimal(entrustAmount));
                    if (remainingShares.compareTo(BigDecimal.ZERO) == 0) {
                        holdingRepository.deleteById(existingHolding.getId());
                        log.info("持仓删除成功(卖完): accountId={}, stockCode={}", accountId, stockCode);
                    } else {
                        BigDecimal totalCost = existingHolding.getCostPrice().multiply(currentShares)
                                .subtract(entrustPrice.multiply(new BigDecimal(entrustAmount)));
                        BigDecimal newCostPrice = totalCost.divide(remainingShares, 4, BigDecimal.ROUND_HALF_UP);
                        
                        existingHolding.setHoldingShares(remainingShares);
                        existingHolding.setCostPrice(newCostPrice);
                        existingHolding.setCurrentPositionAmount(remainingShares.multiply(existingHolding.getCurrentNav()));
                        holdingRepository.updateById(existingHolding);
                        log.info("持仓更新成功(卖出): accountId={}, stockCode={}, 剩余数量={}", 
                                accountId, stockCode, remainingShares);
                    }
                } else {
                    log.warn("持仓不足(卖出): accountId={}, stockCode={}, 当前持仓={}, 卖出数量={}", 
                            accountId, stockCode, currentShares, entrustAmount);
                }
            } else {
                log.warn("无持仓可卖: accountId={}, stockCode={}", accountId, stockCode);
            }
        }
    }
}
