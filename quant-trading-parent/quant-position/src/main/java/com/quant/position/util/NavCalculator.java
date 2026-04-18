package com.quant.position.util;

import com.quant.position.entity.Holding;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class NavCalculator {

    private NavCalculator() {
    }

    public static void calculateProfit(Holding holding, BigDecimal yesterdayNav) {
        BigDecimal holdingShares = holding.getHoldingShares();
        if (holdingShares == null) {
            holdingShares = BigDecimal.ZERO;
        }

        BigDecimal currentNav = holding.getCurrentNav();
        if (currentNav == null) {
            return;
        }

        if (yesterdayNav != null && yesterdayNav.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal todayChangeRate = currentNav
                    .subtract(yesterdayNav)
                    .divide(yesterdayNav, 6, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            holding.setTodayChangeRate(todayChangeRate);

            BigDecimal yesterdayValue = holdingShares.multiply(yesterdayNav);
            BigDecimal todayValue = holdingShares.multiply(currentNav);
            holding.setTodayProfit(todayValue.subtract(yesterdayValue));
        }

        if (holding.getInitialNav() != null && holding.getInitialNav().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal totalProfitRate = currentNav
                    .subtract(holding.getInitialNav())
                    .divide(holding.getInitialNav(), 6, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            holding.setTotalProfitRate(totalProfitRate);
        }

        holding.setTotalProfit(holdingShares
                .multiply(currentNav)
                .subtract(holding.getInitialFund()));

        holding.setCurrentPositionAmount(holdingShares.multiply(currentNav));

        if (holding.getOpenedDate() != null) {
            long days = ChronoUnit.DAYS.between(holding.getOpenedDate(), LocalDate.now());
            holding.setHoldingDays((int) days);
        }

        holding.setCurrDate(LocalDate.now());
    }
}
