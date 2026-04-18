package com.quant.analysis.model.enums;

/**
 * Agent类型枚举
 */
public enum AgentType {
    FUNDAMENTALS_ANALYST("基本面分析师"),
    MARKET_ANALYST("市场分析师"),
    NEWS_ANALYST("新闻分析师"),
    SENTIMENT_ANALYST("舆情分析师"),
    BULL_RESEARCHER("看涨研究员"),
    BEAR_RESEARCHER("看跌研究员"),
    TRADER("交易员"),
    RISK_DEBATOR("风险辩论者");
    
    private final String description;
    
    AgentType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
