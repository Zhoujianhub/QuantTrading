package com.quant.analysis.exception;

/**
 * 分析模块异常
 */
public class AnalysisException extends RuntimeException {
    
    private final String errorCode;
    private final String errorMessage;
    
    public AnalysisException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    
    public AnalysisException(String errorMessage) {
        this("ANALYSIS_ERROR", errorMessage);
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
}
