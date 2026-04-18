package com.quant.analysis.model.enums;

/**
 * 报告类型枚举
 */
public enum ReportType {
    BRIEF("简报"),
    DETAILED("详细报告"),
    INVESTMENT("投资建议");
    
    private final String description;
    
    ReportType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
