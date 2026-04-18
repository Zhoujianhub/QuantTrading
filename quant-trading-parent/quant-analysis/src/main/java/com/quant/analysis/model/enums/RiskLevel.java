package com.quant.analysis.model.enums;

/**
 * 风险等级枚举
 */
public enum RiskLevel {
    LOW("低风险"),
    MEDIUM("中等风险"),
    HIGH("高风险");
    
    private final String description;
    
    RiskLevel(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
