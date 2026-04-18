package com.quant.analysis.model.enums;

/**
 * 投资决策枚举
 */
public enum Decision {
    BUY("买入"),
    SELL("卖出"),
    HOLD("持有");
    
    private final String description;
    
    Decision(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
