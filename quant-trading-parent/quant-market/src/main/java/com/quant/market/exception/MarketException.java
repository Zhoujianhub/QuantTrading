package com.quant.market.exception;

import lombok.Getter;

/**
 * 市场模块自定义异常
 */
@Getter
public class MarketException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /** 错误码 */
    private final int errorCode;
    
    /** 错误消息 */
    private final String errorMessage;
    
    public MarketException(int errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    
    public MarketException(MarketResultCode resultCode) {
        super(resultCode.getMessage());
        this.errorCode = resultCode.getCode();
        this.errorMessage = resultCode.getMessage();
    }
    
    public MarketException(MarketResultCode resultCode, String customMessage) {
        super(customMessage);
        this.errorCode = resultCode.getCode();
        this.errorMessage = customMessage;
    }
}