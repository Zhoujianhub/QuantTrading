package com.quant.analysis.model.enums;

/**
 * 分析状态枚举
 */
public enum AnalysisStatus {
    PENDING("待处理"),
    PROCESSING("处理中"),
    COMPLETED("已完成"),
    FAILED("失败"),
    CANCELLED("已取消");
    
    private final String description;
    
    AnalysisStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
