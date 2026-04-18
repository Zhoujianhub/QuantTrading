package com.quant.analysis.model.enums;

/**
 * 分析维度枚举
 */
public enum Dimension {
    FUNDAMENTALS("基本面"),
    MARKET("市场技术"),
    NEWS("新闻"),
    SENTIMENT("舆情");
    
    private final String description;
    
    Dimension(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
