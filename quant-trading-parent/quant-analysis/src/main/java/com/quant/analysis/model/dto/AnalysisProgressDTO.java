package com.quant.analysis.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 分析进度DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisProgressDTO {
    
    private String requestId;
    private String status;
    private Integer progress;
    private String currentStage;
    private Map<String, String> stages;
    private String errorMessage;
}
