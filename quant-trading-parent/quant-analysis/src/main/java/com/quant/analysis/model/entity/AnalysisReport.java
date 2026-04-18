package com.quant.analysis.model.entity;

import com.quant.analysis.model.enums.Decision;
import com.quant.analysis.model.enums.RiskLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分析报告实体
 */
@Entity
@Table(name = "analysis_report")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisReport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "request_id", unique = true, nullable = false, length = 64)
    private String requestId;
    
    @Column(name = "stock_code", nullable = false, length = 32)
    private String stockCode;
    
    @Column(name = "stock_name", length = 128)
    private String stockName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "decision", length = 16)
    private Decision decision;
    
    @Column(name = "confidence", precision = 5, scale = 4)
    private BigDecimal confidence;
    
    @Column(name = "fundamentals_score", precision = 5, scale = 2)
    private BigDecimal fundamentalsScore;
    
    @Column(name = "market_score", precision = 5, scale = 2)
    private BigDecimal marketScore;
    
    @Column(name = "news_score", precision = 5, scale = 2)
    private BigDecimal newsScore;
    
    @Column(name = "sentiment_score", precision = 5, scale = 2)
    private BigDecimal sentimentScore;
    
    @Column(name = "fundamentals_summary", columnDefinition = "TEXT")
    private String fundamentalsSummary;
    
    @Column(name = "market_summary", columnDefinition = "TEXT")
    private String marketSummary;
    
    @Column(name = "news_summary", columnDefinition = "TEXT")
    private String newsSummary;
    
    @Column(name = "sentiment_summary", columnDefinition = "TEXT")
    private String sentimentSummary;
    
    @Column(name = "bull_argument", columnDefinition = "TEXT")
    private String bullArgument;
    
    @Column(name = "bear_argument", columnDefinition = "TEXT")
    private String bearArgument;
    
    @Column(name = "investment_plan", columnDefinition = "TEXT")
    private String investmentPlan;
    
    @Column(name = "risk_analysis", columnDefinition = "TEXT")
    private String riskAnalysis;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 16)
    private RiskLevel riskLevel;
    
    @Column(name = "final_report", columnDefinition = "TEXT")
    private String finalReport;
    
    @Column(name = "report_data", columnDefinition = "JSON")
    private String reportData;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
