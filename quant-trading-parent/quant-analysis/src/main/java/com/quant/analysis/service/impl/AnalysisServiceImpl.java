package com.quant.analysis.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quant.analysis.executor.AnalysisExecutorClient;
import com.quant.analysis.model.AgentState;
import com.quant.analysis.model.dto.*;
import com.quant.analysis.model.entity.AnalysisReport;
import com.quant.analysis.model.entity.AnalysisRequest;
import com.quant.analysis.model.enums.AnalysisStatus;
import com.quant.analysis.model.enums.Decision;
import com.quant.analysis.model.enums.Dimension;
import com.quant.analysis.repository.AnalysisReportRepository;
import com.quant.analysis.repository.AnalysisRequestRepository;
import com.quant.analysis.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 股票分析服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {

    private final AnalysisRequestRepository requestRepository;
    private final AnalysisReportRepository reportRepository;
    private final AnalysisExecutorClient executorClient;
    private final Gson gson = new Gson();

    private static final DateTimeFormatter REQUEST_ID_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    @Transactional
    public AnalysisRequestDTO submitAnalysis(AnalysisRequestDTO request) {
        String requestId = generateRequestId();
        
        // 构建维度JSON
        String dimensionsJson = request.getDimensions() != null ? 
                gson.toJson(request.getDimensions()) : 
                gson.toJson(Arrays.asList("FUNDAMENTALS", "MARKET", "NEWS", "SENTIMENT"));
        
        // 创建分析请求实体
        AnalysisRequest entity = AnalysisRequest.builder()
                .requestId(requestId)
                .stockCode(request.getStockCode())
                .stockName(request.getStockName())
                .dimensions(dimensionsJson)
                .reportType(request.getReportType())
                .status(AnalysisStatus.PENDING)
                .progress(0)
                .currentStage("SUBMITTED")
                .build();
        
        requestRepository.save(entity);
        
        // 设置返回DTO
        request.setRequestId(requestId);
        request.setStatus("PENDING");
        
        // 异步执行分析
        submitAsync(requestId);
        
        return request;
    }

    /**
     * 异步提交分析任务
     */
    public void submitAsync(String requestId) {
        CompletableFuture.runAsync(() -> {
            try {
                executeAnalysisAsync(requestId);
            } catch (Exception e) {
                log.error("Async analysis execution failed for requestId: {}", requestId, e);
            }
        });
    }

    @Override
    @Transactional
    public String submitBatchAnalysis(BatchAnalysisDTO batchRequest) {
        String batchId = "BATCH-" + generateRequestId();
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (String stockCode : batchRequest.getStockCodes()) {
            final String code = stockCode;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    List<Dimension> dimensions = null;
                    if (batchRequest.getDimensions() != null) {
                        dimensions = new ArrayList<>();
                        for (String dim : batchRequest.getDimensions()) {
                            try {
                                dimensions.add(Dimension.valueOf(dim));
                            } catch (Exception e) {
                                log.warn("Invalid dimension: {}", dim);
                            }
                        }
                    }
                    
                    AnalysisRequestDTO dto = AnalysisRequestDTO.builder()
                            .stockCode(code)
                            .dimensions(dimensions)
                            .reportType(batchRequest.getReportType())
                            .build();
                    submitAnalysis(dto);
                } catch (Exception e) {
                    log.error("Batch analysis failed for stock: {}", code, e);
                }
            });
            futures.add(future);
        }
        
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        return batchId;
    }

    @Override
    public AnalysisProgressDTO getProgress(String requestId) {
        Optional<AnalysisRequest> opt = requestRepository.findByRequestId(requestId);
        if (opt.isEmpty()) {
            return null;
        }
        
        AnalysisRequest entity = opt.get();
        Map<String, String> stages = new LinkedHashMap<>();
        stages.put("SUBMITTED", entity.getStatus() == AnalysisStatus.PENDING ? "COMPLETED" : "");
        stages.put("ANALYZING", "PENDING");
        stages.put("DEBATE", "PENDING");
        stages.put("TRADING", "PENDING");
        stages.put("RISK_ASSESSMENT", "PENDING");
        stages.put("REPORT", entity.getProgress() != null && entity.getProgress() >= 100 ? "COMPLETED" : "PENDING");
        
        return AnalysisProgressDTO.builder()
                .requestId(requestId)
                .status(entity.getStatus() != null ? entity.getStatus().name() : "UNKNOWN")
                .progress(entity.getProgress())
                .currentStage(entity.getCurrentStage())
                .stages(stages)
                .build();
    }

    @Override
    public AnalysisReportDTO getReport(String requestId) {
        Optional<AnalysisReport> opt = reportRepository.findByRequestId(requestId);
        if (opt.isEmpty()) {
            return null;
        }
        
        AnalysisReport entity = opt.get();
        return convertToDTO(entity);
    }

    @Override
    public List<AnalysisReport> getHistory(String stockCode, int page, int pageSize) {
        if (stockCode != null && !stockCode.isEmpty()) {
            return reportRepository.findByStockCodeOrderByCreatedAtDesc(stockCode);
        }
        return reportRepository.findTop10ByOrderByCreatedAtDesc();
    }

    @Override
    public Page<AnalysisReport> getHistoryPage(String stockCode, int page, int pageSize) {
        if (stockCode != null && !stockCode.isEmpty()) {
            return reportRepository.findByStockCodeContainingOrderByCreatedAtDesc(stockCode, PageRequest.of(page, pageSize));
        }
        return reportRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, pageSize));
    }

    @Override
    public Map<String, Object> getAllReports(String stockCode, int page, int pageSize) {
        Page<AnalysisReport> completedPage = getHistoryPage(stockCode, page, pageSize);
        
        List<AnalysisRequest> runningRequests = requestRepository.findByStatusInOrderByCreatedAtDesc(
                Arrays.asList(AnalysisStatus.PENDING, AnalysisStatus.PROCESSING));
        
        List<Map<String, Object>> allItems = new ArrayList<>();
        
        for (AnalysisReport report : completedPage.getContent()) {
            Map<String, Object> item = new HashMap<>();
            item.put("requestId", report.getRequestId());
            item.put("stockCode", report.getStockCode());
            item.put("stockName", report.getStockName());
            item.put("status", "COMPLETED");
            item.put("decision", report.getDecision());
            item.put("confidence", report.getConfidence());
            item.put("riskLevel", report.getRiskLevel());
            item.put("createdAt", report.getCreatedAt());
            item.put("reportId", report.getId());
            allItems.add(item);
        }
        
        for (AnalysisRequest req : runningRequests) {
            if (stockCode == null || stockCode.isEmpty() || req.getStockCode().contains(stockCode)) {
                Map<String, Object> item = new HashMap<>();
                item.put("requestId", req.getRequestId());
                item.put("stockCode", req.getStockCode());
                item.put("stockName", req.getStockName());
                item.put("status", req.getStatus() != null ? req.getStatus().name() : "UNKNOWN");
                item.put("progress", req.getProgress());
                item.put("currentStage", req.getCurrentStage());
                item.put("createdAt", req.getCreatedAt());
                allItems.add(item);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", allItems);
        result.put("total", completedPage.getTotalElements() + runningRequests.size());
        result.put("page", page + 1);
        result.put("pageSize", pageSize);
        
        return result;
    }

    @Override
    public int cleanupInvalidData() {
        List<AnalysisReport> allReports = reportRepository.findAll();
        int count = 0;
        for (AnalysisReport report : allReports) {
            if (report.getDecision() == null && report.getFinalReport() == null) {
                reportRepository.delete(report);
                count++;
                log.info("Deleted invalid report: {}", report.getRequestId());
            }
        }
        return count;
    }

    @Override
    @Async
    @Transactional
    public void executeAnalysisAsync(String requestId) {
        log.info("=== executeAnalysisAsync started for requestId: {} ===", requestId);
        try {
            // 更新状态为处理中
            updateRequestStatus(requestId, AnalysisStatus.PROCESSING, 5, "STARTING");
            
            // 获取请求信息
            AnalysisRequest request = requestRepository.findByRequestId(requestId)
                    .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));
            
            // 构建Agent状态
            AgentState state = AgentState.builder()
                    .requestId(requestId)
                    .stockCode(request.getStockCode())
                    .stockName(request.getStockName())
                    .tradeDate(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))
                    .dimensions(parseDimensions(request.getDimensions()))
                    .status("RUNNING")
                    .progress(0)
                    .build();
            
            // 使用SSE执行分析，实时获取进度更新
            log.info("Starting SSE analysis for requestId: {}", requestId);
            
            AgentState result = executorClient.executeAnalysisWithProgress(state, progressUpdate -> {
                // 只记录进度，不更新数据库（避免异步线程中的事务问题）
                log.info("[Progress] Agent: {}, Progress: {}%, Stage: {}, RequestId: {}", 
                        progressUpdate.getAgent(), 
                        progressUpdate.getProgress(),
                        progressUpdate.getStage(),
                        progressUpdate.getRequestId());
            });
            
            log.info("executorClient returned, about to save report for requestId: {}", requestId);
            
            // 调试: 打印返回结果状态
            log.info("=== Result from executor: requestId={}, status={}, decision={}, confidence={} ===", 
                result.getRequestId(), result.getStatus(), result.getDecision(), result.getConfidence());
            log.info(" fundamentals: {}, market: {}, news: {}, sentiment: {}", 
                result.getFundamentals() != null ? "EXISTS" : "NULL",
                result.getMarketAnalysis() != null ? "EXISTS" : "NULL",
                result.getNewsAnalysis() != null ? "EXISTS" : "NULL",
                result.getSentimentAnalysis() != null ? "EXISTS" : "NULL");
            
            // 保存报告
            try {
                saveReport(result);
                log.info("Report saved successfully for requestId: {}", requestId);
            } catch (Exception e) {
                log.error("Failed to save report for requestId: {}", requestId, e);
            }
            
            // 完成
            updateRequestStatus(requestId, AnalysisStatus.COMPLETED, 100, "COMPLETED");
            log.info("Analysis completed successfully for requestId: {}", requestId);
            
        } catch (Exception e) {
            log.error("Analysis execution failed for requestId: {}", requestId, e);
            updateRequestStatus(requestId, AnalysisStatus.FAILED, 0, "FAILED");
        }
    }
    
    /**
     * 将agent进度映射到数据库进度
     */
    private int mapToDbProgress(String agent, int agentProgress) {
        // 根据agent和进度映射到整体进度
        // Stage 1 (并行): fundamentals, market, news, sentiment -> 10-35%
        // Stage 2 (并行): bull, bear -> 40-60%
        // Stage 3 (并行): trader, risk -> 65-90%
        // Report -> 95-100%
        
        if (agent == null) {
            return agentProgress;
        }
        
        switch (agent) {
            case "fundamentals":
            case "market":
            case "news":
            case "sentiment":
                return 10 + (int)(agentProgress * 0.25); // 10-35%
            case "STAGE_1_START":
                return 10;
            case "bull":
            case "bear":
                return 40 + (int)((agentProgress - 50) * 0.5); // 40-60%
            case "STAGE_2_START":
                return 40;
            case "trader":
            case "risk":
                return 65 + (int)((agentProgress - 75) * 0.6); // 65-90%
            case "STAGE_3_START":
                return 65;
            case "report":
            case "REPORT_START":
                return 90 + (int)((agentProgress - 95) * 2); // 90-100%
            default:
                return agentProgress;
        }
    }

    private void updateRequestStatus(String requestId, AnalysisStatus status, int progress, String stage) {
        requestRepository.findByRequestId(requestId).ifPresent(entity -> {
            entity.setStatus(status);
            entity.setProgress(progress);
            entity.setCurrentStage(stage);
            if (status == AnalysisStatus.COMPLETED || status == AnalysisStatus.FAILED) {
                entity.setCompletedAt(LocalDateTime.now());
            }
            requestRepository.save(entity);
        });
    }

    private void saveReport(AgentState state) {
        log.info("=== saveReport called for requestId: {} ===", state.getRequestId());
        log.info("state.getFundamentalsReport() = {}", state.getFundamentalsReport() != null ? "NOT NULL (" + state.getFundamentalsReport().length() + " chars)" : "NULL");
        log.info("state.getFundamentals() = {}", state.getFundamentals() != null ? "NOT NULL (" + state.getFundamentals().length() + " chars)" : "NULL");
        log.info("state.getMarketReport() = {}", state.getMarketReport() != null ? "NOT NULL (" + state.getMarketReport().length() + " chars)" : "NULL");
        log.info("state.getMarketAnalysis() = {}", state.getMarketAnalysis() != null ? "NOT NULL (" + state.getMarketAnalysis().length() + " chars)" : "NULL");
        log.info("state.getNewsReport() = {}", state.getNewsReport() != null ? "NOT NULL (" + state.getNewsReport().length() + " chars)" : "NULL");
        log.info("state.getNewsAnalysis() = {}", state.getNewsAnalysis() != null ? "NOT NULL (" + state.getNewsAnalysis().length() + " chars)" : "NULL");
        log.info("state.getSentimentReport() = {}", state.getSentimentReport() != null ? "NOT NULL (" + state.getSentimentReport().length() + " chars)" : "NULL");
        log.info("state.getSentimentAnalysis() = {}", state.getSentimentAnalysis() != null ? "NOT NULL (" + state.getSentimentAnalysis().length() + " chars)" : "NULL");
        log.info("state.getDecision() = {}", state.getDecision());
        log.info("state.getFinalDecision() = {}", state.getFinalDecision());
        log.info("state.getConfidence() = {}", state.getConfidence());
        log.info("state.getFinalReport() = {}", state.getFinalReport() != null ? "NOT NULL (" + state.getFinalReport().length() + " chars)" : "NULL");
        log.info("state.getRiskLevel() = {}", state.getRiskLevel());
        
        String fundamentalsContent = state.getFundamentalsReport() != null ? state.getFundamentalsReport() : state.getFundamentals();
        String marketContent = state.getMarketReport() != null ? state.getMarketReport() : state.getMarketAnalysis();
        String newsContent = state.getNewsReport() != null ? state.getNewsReport() : state.getNewsAnalysis();
        String sentimentContent = state.getSentimentReport() != null ? state.getSentimentReport() : state.getSentimentAnalysis();
        
        AnalysisReport.AnalysisReportBuilder builder = AnalysisReport.builder()
                .requestId(state.getRequestId())
                .stockCode(state.getStockCode())
                .stockName(state.getStockName());
        
        if (state.getDecision() != null) {
            try {
                builder.decision(Decision.valueOf(state.getDecision()));
            } catch (Exception e) {
                try {
                    builder.decision(Decision.valueOf(state.getFinalDecision()));
                } catch (Exception ex) {
                    log.warn("Invalid decision value: {} or {}", state.getDecision(), state.getFinalDecision());
                }
            }
        }
        
        if (state.getConfidence() != null) {
            try {
                builder.confidence(new BigDecimal(state.getConfidence().toString()));
            } catch (Exception e) {
                log.warn("Invalid confidence value: {}", state.getConfidence());
            }
        }
        
        builder.fundamentalsSummary(fundamentalsContent)
                .fundamentalsScore(state.getFundamentalsScore() != null ? 
                        new BigDecimal(state.getFundamentalsScore().toString()) : null)
                .marketSummary(marketContent)
                .marketScore(state.getMarketScore() != null ? 
                        new BigDecimal(state.getMarketScore().toString()) : null)
                .newsSummary(newsContent)
                .newsScore(state.getNewsScore() != null ? 
                        new BigDecimal(state.getNewsScore().toString()) : null)
                .sentimentSummary(sentimentContent)
                .sentimentScore(state.getSentimentScore() != null ? 
                        new BigDecimal(state.getSentimentScore().toString()) : null)
                .bullArgument(state.getBullArgument())
                .bearArgument(state.getBearArgument())
                .investmentPlan(state.getTraderRecommendation())
                .riskAnalysis(state.getRiskAnalysis())
                .riskLevel(state.getRiskLevel() != null ? 
                        com.quant.analysis.model.enums.RiskLevel.valueOf(state.getRiskLevel()) : null)
                .finalReport(state.getFinalReport());
        
        try {
            AnalysisReport saved = reportRepository.save(builder.build());
            log.info("Report saved successfully: id={}, requestId={}", saved.getId(), saved.getRequestId());
        } catch (Exception e) {
            log.error("Failed to save report to database", e);
        }
    }

    private AnalysisReportDTO convertToDTO(AnalysisReport entity) {
        return AnalysisReportDTO.builder()
                .requestId(entity.getRequestId())
                .stockCode(entity.getStockCode())
                .stockName(entity.getStockName())
                .decision(entity.getDecision())
                .confidence(entity.getConfidence())
                .decisionDate(entity.getCreatedAt() != null ? 
                        entity.getCreatedAt().format(DateTimeFormatter.ISO_DATE) : null)
                .bullArgument(entity.getBullArgument())
                .bearArgument(entity.getBearArgument())
                .investmentPlan(entity.getInvestmentPlan())
                .riskAnalysis(entity.getRiskAnalysis())
                .fundamentalsSummary(entity.getFundamentalsSummary())
                .fundamentalsScore(entity.getFundamentalsScore())
                .fundamentalsReport(entity.getFundamentalsSummary())
                .marketSummary(entity.getMarketSummary())
                .marketScore(entity.getMarketScore())
                .marketReport(entity.getMarketSummary())
                .newsSummary(entity.getNewsSummary())
                .newsScore(entity.getNewsScore())
                .newsReport(entity.getNewsSummary())
                .sentimentSummary(entity.getSentimentSummary())
                .sentimentScore(entity.getSentimentScore())
                .sentimentReport(entity.getSentimentSummary())
                .finalReport(entity.getFinalReport())
                .build();
    }

    private String generateRequestId() {
        return "ANALYSIS-" + LocalDateTime.now().format(REQUEST_ID_FORMATTER) + 
               "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private List<String> parseDimensions(String dimensionsJson) {
        if (dimensionsJson == null || dimensionsJson.isEmpty()) {
            return Arrays.asList("FUNDAMENTALS", "MARKET", "NEWS", "SENTIMENT");
        }
        try {
            return gson.fromJson(dimensionsJson, new TypeToken<List<String>>(){}.getType());
        } catch (Exception e) {
            log.warn("Failed to parse dimensions: {}", dimensionsJson);
            return Arrays.asList("FUNDAMENTALS", "MARKET", "NEWS", "SENTIMENT");
        }
    }
}
