package com.quant.market.model;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 历史K线数据模型
 */
@Data
public class HistoricalKline implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 股票代码 */
    private String stockCode;
    
    /** 交易日期 */
    private String tradeDate;
    
    /** 开盘价 */
    private BigDecimal open;
    
    /** 最高价 */
    private BigDecimal high;
    
    /** 最低价 */
    private BigDecimal low;
    
    /** 收盘价 */
    private BigDecimal close;
    
    /** 成交量 */
    private Long volume;
    
    /** 成交额 */
    private BigDecimal amount;
    
    /** 换手率 */
    private BigDecimal turnoverRate;
    
    /** 复权类型: None/Forward/Backward */
    private String adjustType;
}
