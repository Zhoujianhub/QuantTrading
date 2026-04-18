package com.quant.account.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quant.account.dto.request.LoginRequest;
import com.quant.account.dto.request.RegisterRequest;
import com.quant.account.dto.request.PositionQueryRequest;
import com.quant.account.dto.response.AccountInfoResponse;
import com.quant.account.dto.response.AssetResponse;
import com.quant.account.dto.response.LoginResponse;
import com.quant.account.dto.response.PositionResponse;
import com.quant.account.entity.Client;
import com.quant.account.entity.Fund;
import com.quant.account.entity.Stock;
import com.quant.account.entity.Stockholder;
import com.quant.account.repository.ClientRepository;
import com.quant.account.repository.FundRepository;
import com.quant.account.repository.StockRepository;
import com.quant.account.repository.StockholderRepository;
import com.quant.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final ClientRepository clientRepository;
    private final FundRepository fundRepository;
    private final StockholderRepository stockholderRepository;
    private final StockRepository stockRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public LoginResponse login(LoginRequest request) {
        Client client = clientRepository.selectById(request.getClientId());
        if (client == null) {
            throw new RuntimeException("客户不存在");
        }

        if (!client.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        Fund fund = fundRepository.selectOne(new LambdaQueryWrapper<Fund>()
                .eq(Fund::getClientId, request.getClientId()));
        if (fund == null) {
            throw new RuntimeException("资金账户不存在");
        }

        LoginResponse response = new LoginResponse();
        response.setClientId(client.getClientId());
        response.setClientName(client.getClientName());
        response.setFundAccount(fund.getFundAccount());
        response.setBeginBalance(fund.getBeginBalance());
        response.setCurrentBalance(fund.getCurrentBalance());
        return response;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        String today = LocalDate.now().format(DATE_FORMATTER);

        long clientCount = clientRepository.selectCount(null);
        String clientId = String.format("%05d", clientCount + 1);

        Client client = new Client();
        client.setClientId(clientId);
        client.setClientName(request.getClientName());
        client.setOpenDate(today);
        client.setPassword(request.getPassword());
        clientRepository.insert(client);

        long fundCount = fundRepository.selectCount(null);
        String fundAccount = String.format("%08d", 80000001L + fundCount);

        Fund fund = new Fund();
        fund.setClientId(clientId);
        fund.setClientName(request.getClientName());
        fund.setFundAccount(fundAccount);
        fund.setBeginBalance(request.getBeginBalance());
        fund.setCurrentBalance(request.getBeginBalance());
        fund.setAssetProp("现金");
        fundRepository.insert(fund);

        createStockholders(clientId, fundAccount, request.getClientName(), today);
    }

    private void createStockholders(String clientId, String fundAccount, String clientName, String openDate) {
        List<Stockholder> stockholders = new ArrayList<>();

        Stockholder shHolder = new Stockholder();
        shHolder.setClientId(clientId);
        shHolder.setClientName(clientName);
        shHolder.setFundAccount(fundAccount);
        shHolder.setStockAccount("00001");
        shHolder.setExchangeType(0);
        shHolder.setOpenDate(openDate);
        shHolder.setHolderKind(0);
        stockholders.add(shHolder);

        Stockholder szHolder = new Stockholder();
        szHolder.setClientId(clientId);
        szHolder.setClientName(clientName);
        szHolder.setFundAccount(fundAccount);
        szHolder.setStockAccount("10001");
        szHolder.setExchangeType(1);
        szHolder.setOpenDate(openDate);
        szHolder.setHolderKind(0);
        stockholders.add(szHolder);

        for (Stockholder holder : stockholders) {
            stockholderRepository.insert(holder);
        }
    }

    @Override
    public AccountInfoResponse getAccountInfo(String clientId) {
        Client client = clientRepository.selectById(clientId);
        if (client == null) {
            throw new RuntimeException("客户不存在");
        }

        Fund fund = fundRepository.selectOne(new LambdaQueryWrapper<Fund>()
                .eq(Fund::getClientId, clientId));

        List<Stockholder> stockholders = stockholderRepository.selectList(
                new LambdaQueryWrapper<Stockholder>().eq(Stockholder::getClientId, clientId));

        AccountInfoResponse response = new AccountInfoResponse();
        response.setClientId(client.getClientId());
        response.setClientName(client.getClientName());

        if (fund != null) {
            AccountInfoResponse.FundInfo fundInfo = new AccountInfoResponse.FundInfo();
            fundInfo.setFundAccount(fund.getFundAccount());
            fundInfo.setBeginBalance(fund.getBeginBalance());
            fundInfo.setCurrentBalance(fund.getCurrentBalance());
            fundInfo.setAssetProp(fund.getAssetProp());
            response.setFund(fundInfo);
        }

        List<AccountInfoResponse.StockholderInfo> holderInfos = stockholders.stream()
                .map(h -> {
                    AccountInfoResponse.StockholderInfo info = new AccountInfoResponse.StockholderInfo();
                    info.setStockAccount(h.getStockAccount());
                    info.setExchangeType(h.getExchangeType());
                    info.setExchangeName(getExchangeName(h.getExchangeType()));
                    info.setHolderKind(h.getHolderKind());
                    info.setOpenDate(h.getOpenDate());
                    return info;
                })
                .collect(Collectors.toList());
        response.setStockholders(holderInfos);

        return response;
    }

    @Override
    public List<PositionResponse> getPositions(PositionQueryRequest request) {
        LambdaQueryWrapper<Stock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Stock::getClientId, request.getClientId());

        if (request.getFundAccount() != null && !request.getFundAccount().isEmpty()) {
            wrapper.eq(Stock::getFundAccount, request.getFundAccount());
        }
        if (request.getStockAccount() != null && !request.getStockAccount().isEmpty()) {
            wrapper.eq(Stock::getStockAccount, request.getStockAccount());
        }
        if (request.getExchangeType() != null) {
            wrapper.eq(Stock::getExchangeType, request.getExchangeType());
        }

        List<Stock> stocks = stockRepository.selectList(wrapper);

        return stocks.stream().map(s -> {
            PositionResponse pos = new PositionResponse();
            pos.setFundAccount(s.getFundAccount());
            pos.setStockAccount(s.getStockAccount());
            pos.setExchangeType(s.getExchangeType());
            pos.setExchangeName(getExchangeName(s.getExchangeType()));
            pos.setStockCode(s.getStockCode());
            pos.setStockName(s.getStockName());
            pos.setStockType(s.getStockType());
            pos.setBeginAmount(s.getBeginAmount());
            pos.setCurrentAmount(s.getCurrentAmount());
            pos.setSumBuyAmount(s.getSumBuyAmount());
            pos.setSumBuyBalance(s.getSumBuyBalance());
            pos.setSumSellAmount(s.getSumSellAmount());
            pos.setSumSellBalance(s.getSumSellBalance());
            pos.setCostPrice(s.getCostPrice());
            return pos;
        }).collect(Collectors.toList());
    }

    @Override
    public AssetResponse getAssets(String clientId) {
        Fund fund = fundRepository.selectOne(new LambdaQueryWrapper<Fund>()
                .eq(Fund::getClientId, clientId));
        if (fund == null) {
            throw new RuntimeException("资金账户不存在");
        }

        List<Stock> stocks = stockRepository.selectList(
                new LambdaQueryWrapper<Stock>().eq(Stock::getClientId, clientId));

        AssetResponse response = new AssetResponse();
        response.setClientId(clientId);
        response.setCashAsset(fund.getCurrentBalance());
        response.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        BigDecimal totalStockValue = BigDecimal.ZERO;
        List<AssetResponse.PositionDetail> details = new ArrayList<>();

        for (Stock stock : stocks) {
            AssetResponse.PositionDetail detail = new AssetResponse.PositionDetail();
            detail.setStockCode(stock.getStockCode());
            detail.setStockName(stock.getStockName());
            detail.setCurrentAmount(stock.getCurrentAmount());
            detail.setCostPrice(stock.getCostPrice());

            BigDecimal currentPrice = stock.getCostPrice();
            detail.setCurrentPrice(currentPrice);

            BigDecimal marketValue = stock.getCurrentAmount().multiply(currentPrice);
            detail.setMarketValue(marketValue);

            BigDecimal costValue = stock.getCurrentAmount().multiply(stock.getCostPrice());
            BigDecimal profitLoss = marketValue.subtract(costValue);
            detail.setProfitLoss(profitLoss);

            if (stock.getCostPrice().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal profitLossRate = profitLoss.divide(costValue, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                detail.setProfitLossRate(profitLossRate);
            } else {
                detail.setProfitLossRate(BigDecimal.ZERO);
            }

            totalStockValue = totalStockValue.add(marketValue);
            details.add(detail);
        }

        response.setStockAsset(totalStockValue);
        response.setTotalAsset(fund.getCurrentBalance().add(totalStockValue));
        response.setPositions(details);

        return response;
    }

    private String getExchangeName(Integer exchangeType) {
        if (exchangeType == null) return "未知";
        return switch (exchangeType) {
            case 0 -> "上海";
            case 1 -> "深圳";
            case 3 -> "北京";
            default -> "未知";
        };
    }
}
