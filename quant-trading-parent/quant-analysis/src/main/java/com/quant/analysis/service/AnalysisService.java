package com.quant.analysis.service;

import com.quant.analysis.model.dto.*;
import com.quant.analysis.model.entity.AnalysisReport;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * 分析服务接口
 */
public interface AnalysisService {
    
    /**
     * 提交股票分析请求
     */
    AnalysisRequestDTO submitAnalysis(AnalysisRequestDTO request);
    
    /**
     * 提交批量分析请求
     */
    String submitBatchAnalysis(BatchAnalysisDTO request);
    
    /**
     * 获取分析进度
     */
    AnalysisProgressDTO getProgress(String requestId);
    
    /**
     * 获取分析报告
     */
    AnalysisReportDTO getReport(String requestId);
    
    /**
     * 获取分析历史（已完成报告）
     */
    List<AnalysisReport> getHistory(String stockCode, int page, int pageSize);
    
    /**
     * 获取分析历史（分页）
     */
    Page<AnalysisReport> getHistoryPage(String stockCode, int page, int pageSize);
    
    /**
     * 获取所有分析报告（包含正在进行的）
     */
    Map<String, Object> getAllReports(String stockCode, int page, int pageSize);
    
    /**
     * 清理无效的分析数据
     */
    int cleanupInvalidData();
    
    /**
     * 异步执行分析
     */
    void executeAnalysisAsync(String requestId);
}
