package com.quant.analysis.model.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量分析请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchAnalysisDTO {
    
    @NotEmpty(message = "股票代码列表不能为空")
    private List<String> stockCodes;
    
    private List<String> dimensions;
    
    private String reportType;
}
