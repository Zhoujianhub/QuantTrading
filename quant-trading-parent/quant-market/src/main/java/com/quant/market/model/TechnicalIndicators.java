package com.quant.market.model;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 技术指标数据模型
 */
@Data
public class TechnicalIndicators implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 股票代码 */
    private String stockCode;
    
    /** 日期 */
    private String date;
    
    /** 5日均线 */
    private BigDecimal ma5;
    
    /** 10日均线 */
    private BigDecimal ma10;
    
    /** 20日均线 */
    private BigDecimal ma20;
    
    /** 60日均线 */
    private BigDecimal ma60;
    
    /** RSI(6) */
    private BigDecimal rsi6;
    
    /** RSI(12) */
    private BigDecimal rsi12;
    
    /** RSI(24) */
    private BigDecimal rsi24;
    
    /** MACD */
    private BigDecimal macd;
    
    /** MACD Signal */
    private BigDecimal macdSignal;
    
    /** MACD Hist */
    private BigDecimal macdHist;
    
    /** KDJ K值 */
    private BigDecimal kdjK;
    
    /** KDJ D值 */
    private BigDecimal kdjD;
    
    /** KDJ J值 */
    private BigDecimal kdjJ;
    
    /** 布林带上轨 */
    private BigDecimal bollUpper;
    
    /** 布林带中轨 */
    private BigDecimal bollMiddle;
    
    /** 布林带下轨 */
    private BigDecimal bollLower;
}
