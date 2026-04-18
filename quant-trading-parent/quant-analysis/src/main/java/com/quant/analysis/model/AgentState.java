package com.quant.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Agent执行状态
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentState implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // ==================== 基础信息 ====================
    private String requestId;           // 请求ID
    private String stockCode;           // 股票代码
    private String stockName;           // 股票名称
    private String tradeDate;          // 交易日期
    private String sender;              // 发送消息的Agent
    
    // ==================== 分析维度 ====================
    private List<String> dimensions;   // 启用的分析维度
    
    // ==================== 分析师报告 ====================
    private String fundamentalsReport;  // 基本面分析报告
    private String fundamentals;  // 基本面分析（别名）
    private Double fundamentalsScore;   // 基本面评分
    private String marketReport;        // 市场技术分析报告
    private String marketAnalysis;        // 市场技术分析（别名）
    private Double marketScore;         // 市场评分
    private String newsReport;         // 新闻分析报告
    private String newsAnalysis;         // 新闻分析（别名）
    private Double newsScore;           // 新闻评分
    private String sentimentReport;     // 舆情分析报告
    private String sentimentAnalysis;     // 舆情分析（别名）
    private Double sentimentScore;      // 舆情评分
    
    // ==================== 研究辩论 ====================
    private String bullArgument;       // 看涨论证
    private String bearArgument;       // 看跌论证
    private String debateHistory;       // 辩论历史
    
    // ==================== 交易决策 ====================
    private String investmentPlan;      // 投资计划
    private String traderRecommendation; // 交易员建议
    
    // ==================== 风险评估 ====================
    private String riskAnalysis;       // 风险分析
    private String riskLevel;         // 风险等级 LOW/MEDIUM/HIGH
    private String conservativeRiskAnalysis;  // 保守风险分析
    private String aggressiveRiskAnalysis;    // 激进风险分析
    private String neutralRiskAnalysis;      // 中性风险分析
    
    // ==================== 最终输出 ====================
    private String finalReport;        // 最终分析报告
    private String decision;            // 决策 BUY/SELL/HOLD
    private String finalDecision;       // 最终决策（别名）
    private Double confidence;         // 置信度 0.0-1.0
    
    // ==================== 状态跟踪 ====================
    private String currentStage;       // 当前阶段
    private Integer progress;           // 进度 0-100
    private String status;             // 状态 PENDING/RUNNING/COMPLETED/FAILED
    private String errorMessage;      // 错误信息
}
