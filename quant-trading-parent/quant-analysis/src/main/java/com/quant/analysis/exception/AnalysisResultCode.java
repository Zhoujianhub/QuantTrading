package com.quant.analysis.exception;

/**
 * 分析模块错误码枚举
 */
public enum AnalysisResultCode {
    
    SUCCESS("0", "成功"),
    PARAM_INVALID("A001", "参数无效"),
    REQUEST_NOT_FOUND("A002", "请求不存在"),
    REPORT_NOT_FOUND("A003", "报告不存在"),
    EXECUTOR_ERROR("A004", "执行器错误"),
    EXECUTOR_TIMEOUT("A005", "执行超时"),
    EXECUTOR_UNAVAILABLE("A006", "执行器不可用"),
    ANALYSIS_FAILED("A007", "分析失败"),
    UNKNOWN_ERROR("A999", "未知错误");
    
    private final String code;
    private final String message;
    
    AnalysisResultCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}
