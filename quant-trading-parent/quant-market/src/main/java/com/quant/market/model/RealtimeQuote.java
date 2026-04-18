package com.quant.market.model;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 实时行情数据模型
 */
@Data
public class RealtimeQuote implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 股票代码 */
    private String stockCode;
    
    /** 股票名称 */
    private String stockName;
    
    /** 当前价格 */
    private BigDecimal currentPrice;
    
    /** 涨跌额 */
    private BigDecimal change;
    
    /** 涨跌幅% */
    private BigDecimal changePercent;
    
    /** 开盘价 */
    private BigDecimal open;
    
    /** 最高价 */
    private BigDecimal high;
    
    /** 最低价 */
    private BigDecimal low;
    
    /** 昨收价 */
    private BigDecimal previousClose;
    
    /** 成交量 */
    private Long volume;
    
    /** 成交额 */
    private BigDecimal amount;
    
    /** 买一价 */
    private BigDecimal bid1Price;
    
    /** 买一量 */
    private Long bid1Volume;
    
    /** 卖一价 */
    private BigDecimal ask1Price;
    
    /** 卖一量 */
    private Long ask1Volume;
    
    /** 更新时间 */
    private String updateTime;
    
    /** 市场 SH/SZ */
    private String market;
    
    /** 数据来源 */
    private String source;
    
    /** 是否来自缓存 */
    private Boolean cached;
}
