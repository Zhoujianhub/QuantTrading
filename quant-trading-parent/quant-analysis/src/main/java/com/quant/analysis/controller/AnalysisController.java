package com.quant.analysis.controller;

import com.quant.analysis.model.dto.*;
import com.quant.analysis.model.entity.AnalysisReport;
import com.quant.analysis.service.AnalysisService;
import com.quant.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 分析控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
public class AnalysisController {
    
    private final AnalysisService analysisService;
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * 搜索股票
     */
    @GetMapping("/stocks/search")
    public Result<Map<String, String>> searchStocks(@RequestParam String keyword) {
        try {
            String url = "https://searchapi.eastmoney.com/api/suggest/get?input=" + keyword + "&type=14&token=D43BF722C8E33BDC906FB84D85E326E8&count=5";
            String response = restTemplate.getForObject(url, String.class);
            
            Map<String, String> result = new HashMap<>();
            if (response != null && response.contains("\"SecurityCode\"")) {
                int start = response.indexOf("\"SecurityCode\":\"") + 16;
                int end = response.indexOf("\"", start);
                String stockCode = response.substring(start, end);
                
                start = response.indexOf("\"SecurityName\":\"") + 15;
                end = response.indexOf("\"", start);
                String stockName = response.substring(start, end);
                
                result.put("stockCode", stockCode);
                result.put("stockName", stockName);
            }
            
            if (result.isEmpty()) {
                result.put("stockCode", keyword);
                result.put("stockName", "股票-" + keyword);
            }
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("Search stocks failed: {}", keyword, e);
            Map<String, String> fallback = new HashMap<>();
            fallback.put("stockCode", keyword);
            fallback.put("stockName", "股票-" + keyword);
            return Result.success(fallback);
        }
    }
    
    /**
     * 获取热门股票
     */
    @GetMapping("/stocks/hot")
    public Result<List<Map<String, String>>> getHotStocks() {
        try {
            List<Map<String, String>> hotStocks = new ArrayList<>();
            String[] defaultStocks = {"600519", "000858", "601318", "600036", "000333"};
            String[] defaultNames = {"贵州茅台", "五粮液", "中国平安", "招商银行", "美的集团"};
            
            for (int i = 0; i < defaultStocks.length; i++) {
                Map<String, String> stock = new HashMap<>();
                stock.put("stockCode", defaultStocks[i]);
                stock.put("stockName", defaultNames[i]);
                hotStocks.add(stock);
            }
            
            return Result.success(hotStocks);
        } catch (Exception e) {
            log.error("Get hot stocks failed", e);
            return Result.error("获取热门股票失败: " + e.getMessage());
        }
    }
    
    /**
     * 提交股票分析请求
     */
    @PostMapping("/stock/{stockCode}")
    public Result<AnalysisRequestDTO> analyzeStock(
            @PathVariable String stockCode,
            @RequestBody(required = false) AnalysisRequestDTO request) {
        try {
            if (request == null) {
                request = new AnalysisRequestDTO();
            }
            request.setStockCode(stockCode);
            AnalysisRequestDTO result = analysisService.submitAnalysis(request);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Stock analysis failed: {}", stockCode, e);
            return Result.error("分析失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量分析
     */
    @PostMapping("/batch")
    public Result<String> batchAnalyze(@RequestBody @Valid BatchAnalysisDTO request) {
        try {
            String batchId = analysisService.submitBatchAnalysis(request);
            return Result.success(batchId);
        } catch (Exception e) {
            log.error("Batch analysis failed", e);
            return Result.error("批量分析失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取分析进度
     */
    @GetMapping("/progress/{requestId}")
    public Result<AnalysisProgressDTO> getProgress(@PathVariable String requestId) {
        try {
            AnalysisProgressDTO progress = analysisService.getProgress(requestId);
            if (progress == null) {
                return Result.error("请求不存在: " + requestId);
            }
            return Result.success(progress);
        } catch (Exception e) {
            log.error("Get progress failed: {}", requestId, e);
            return Result.error("获取进度失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取分析报告
     */
    @GetMapping("/report/{requestId}")
    public Result<AnalysisReportDTO> getReport(@PathVariable String requestId) {
        try {
            AnalysisReportDTO report = analysisService.getReport(requestId);
            if (report == null) {
                return Result.error("报告不存在: " + requestId);
            }
            return Result.success(report);
        } catch (Exception e) {
            log.error("Get report failed: {}", requestId, e);
            return Result.error("获取报告失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取分析历史（分页）
     */
    @GetMapping("/history")
    public Result<Map<String, Object>> getHistory(
            @RequestParam(required = false) String stockCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            Page<AnalysisReport> historyPage = analysisService.getHistoryPage(stockCode, page - 1, pageSize);
            Map<String, Object> result = new HashMap<>();
            result.put("records", historyPage.getContent());
            result.put("total", historyPage.getTotalElements());
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", historyPage.getTotalPages());
            return Result.success(result);
        } catch (Exception e) {
            log.error("Get history failed", e);
            return Result.error("获取历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有分析报告（包含正在进行的）
     */
    @GetMapping("/reports/all")
    public Result<Map<String, Object>> getAllReports(
            @RequestParam(required = false) String stockCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            Map<String, Object> result = analysisService.getAllReports(stockCode, page - 1, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Get all reports failed", e);
            return Result.error("获取报告列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 清理无效的分析数据
     */
    @DeleteMapping("/cleanup")
    public Result<Integer> cleanupInvalidData() {
        try {
            int count = analysisService.cleanupInvalidData();
            return Result.success(count);
        } catch (Exception e) {
            log.error("Cleanup failed", e);
            return Result.error("清理失败: " + e.getMessage());
        }
    }
}
