package com.quant.analysis.model.dto;

import com.quant.analysis.model.enums.Decision;
import com.quant.analysis.model.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 分析报告DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisReportDTO {
    
    private String requestId;
    private String stockCode;
    private String stockName;
    private Decision decision;
    private BigDecimal confidence;
    private String decisionDate;
    private Map<String, ReportSection> reports;
    private String bullArgument;
    private String bearArgument;
    private String investmentPlan;
    private String riskAnalysis;
    private RiskLevel riskLevel;
    
    private String fundamentalsSummary;
    private BigDecimal fundamentalsScore;
    private String fundamentalsReport;
    private String marketSummary;
    private BigDecimal marketScore;
    private String marketReport;
    private String newsSummary;
    private BigDecimal newsScore;
    private String newsReport;
    private String sentimentSummary;
    private BigDecimal sentimentScore;
    private String sentimentReport;
    private String finalReport;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportSection {
        private String summary;
        private BigDecimal score;
        private Object highlights;
    }
}
