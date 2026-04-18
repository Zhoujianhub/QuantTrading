package com.quant.trade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quant.trade.entity.Entrust;
import com.quant.trade.mapper.EntrustMapper;
import com.quant.trade.service.EntrustService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class EntrustServiceImpl extends ServiceImpl<EntrustMapper, Entrust> implements EntrustService {

    private static final AtomicLong entrustIdCounter = new AtomicLong(System.currentTimeMillis() % 100000);

    @Override
    public String createEntrust(Entrust entrust) {
        LocalDateTime now = LocalDateTime.now();
        entrust.setInitDate(now.toLocalDate());
        entrust.setCurrDate(now.toLocalDate());
        
        long id = entrustIdCounter.incrementAndGet();
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String entrustNo = dateStr + String.format("%05d", id % 100000);
        entrust.setEntrustNo(entrustNo);
        
        if (entrust.getFundAccount() == null || entrust.getFundAccount().isEmpty()) {
            entrust.setFundAccount(entrust.getClientId());
        }
        if (entrust.getExchangeType() == null) {
            entrust.setExchangeType(1);
        }
        if (entrust.getStockAccount() == null || entrust.getStockAccount().isEmpty()) {
            entrust.setStockAccount(entrust.getClientId());
        }
        if (entrust.getEntrustBs() == null) {
            entrust.setEntrustBs("BUY".equalsIgnoreCase(entrust.getDirection()) ? 1 : 2);
        }
        
        entrust.setEntrustStatus(3);
        entrust.setBusinessAmount(entrust.getEntrustAmount());
        entrust.setBusinessPrice(entrust.getEntrustPrice());
        entrust.setCreatedAt(now);
        entrust.setUpdatedAt(now);
        
        save(entrust);
        return entrustNo;
    }

    @Override
    public List<Entrust> getEntrustsByClientId(String clientId) {
        return lambdaQuery().eq(Entrust::getClientId, clientId)
                .orderByDesc(Entrust::getCreatedAt)
                .list();
    }
}