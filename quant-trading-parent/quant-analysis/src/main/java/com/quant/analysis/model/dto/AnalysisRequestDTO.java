package com.quant.analysis.model.dto;

import com.quant.analysis.model.enums.Dimension;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 股票分析请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequestDTO {
    
    @NotBlank(message = "股票代码不能为空")
    private String stockCode;
    
    private String stockName;
    
    private List<Dimension> dimensions;
    
    private String reportType;
    
    private String tradeDate;
    
    private String requestId;
    
    private String status;
    
    private String accountId;
}
