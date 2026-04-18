package com.quant.market.exception;

import lombok.Getter;

/**
 * 市场模块错误码枚举
 */
@Getter
public enum MarketResultCode {
    
    SUCCESS(1000, "成功"),
    
    PARAM_INVALID(1001, "参数无效"),
    
    DATA_SOURCE_UNAVAILABLE(1002, "数据源不可用"),
    
    NETWORK_ERROR(1003, "网络请求失败"),
    
    CACHE_ERROR(1004, "缓存操作失败"),
    
    PARSE_ERROR(1005, "数据解析失败"),
    
    RATE_LIMIT_EXCEEDED(1006, "请求频率超限"),
    
    UNKNOWN_ERROR(1099, "未知错误");
    
    private final int code;
    private final String message;
    
    MarketResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public static MarketResultCode fromCode(int code) {
        for (MarketResultCode resultCode : values()) {
            if (resultCode.code == code) {
                return resultCode;
            }
        }
        return UNKNOWN_ERROR;
    }
}