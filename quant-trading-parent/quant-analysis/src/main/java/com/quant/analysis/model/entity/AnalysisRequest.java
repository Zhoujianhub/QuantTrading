package com.quant.analysis.model.entity;

import com.quant.analysis.model.enums.AnalysisStatus;
import com.quant.analysis.model.enums.Dimension;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 分析请求实体
 */
@Entity
@Table(name = "analysis_request")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "request_id", unique = true, nullable = false, length = 64)
    private String requestId;
    
    @Column(name = "stock_code", nullable = false, length = 32)
    private String stockCode;
    
    @Column(name = "stock_name", length = 128)
    private String stockName;
    
    @Column(name = "batch_id", length = 64)
    private String batchId;
    
    @Column(name = "dimensions", columnDefinition = "JSON")
    private String dimensions;
    
    @Column(name = "report_type", length = 16)
    private String reportType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16)
    private AnalysisStatus status;
    
    @Column(name = "progress")
    private Integer progress;
    
    @Column(name = "current_stage", length = 32)
    private String currentStage;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
