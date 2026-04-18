package com.quant.analysis.executor;

import com.google.gson.Gson;
import com.quant.analysis.model.AgentState;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Python Agent执行器客户端
 */
@Slf4j
@Component
public class AnalysisExecutorClient {
    
    private final OkHttpClient httpClient;
    private final Gson gson = new Gson();
    
    @Value("${quant.analysis.executor.url:http://localhost:5000}")
    private String executorUrl;
    
    @Value("${quant.analysis.executor.timeout:600000}")
    private long timeout;
    
    public AnalysisExecutorClient() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(600, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * 异步提交分析任务
     */
    public void submitAsync(AgentState state) {
        CompletableFuture.runAsync(() -> {
            try {
                executeAnalysis(state);
            } catch (Exception e) {
                log.error("Async analysis execution failed", e);
            }
        });
    }
    
    /**
     * 同步执行分析
     * 支持并行执行，进度会实时输出到日志
     */
    public AgentState executeAnalysis(AgentState state) {
        try {
            MediaType mediaType = MediaType.parse("application/json");
            String json = gson.toJson(state);
            
            RequestBody body = RequestBody.create(json, mediaType);
            Request request = new Request.Builder()
                    .url(executorUrl + "/api/execute")
                    .post(body)
                    .build();
            
            log.info("Executing analysis for requestId: {}, stockCode: {}", 
                    state.getRequestId(), state.getStockCode());
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Executor returned: " + response);
                }
                
                String responseBody = response.body() != null ? response.body().string() : "";
                log.info("Analysis execution completed: {}, response length: {} chars", 
                        state.getRequestId(), responseBody.length());
                
                return gson.fromJson(responseBody, AgentState.class);
            }
        } catch (Exception e) {
            log.error("Analysis execution failed: {}", state.getRequestId(), e);
            state.setStatus("FAILED");
            state.setErrorMessage(e.getMessage());
            return state;
        }
    }
    
    /**
     * 执行分析并通过回调报告进度
     * 注意: 当前实现不支持真正的SSE进度推送，仅记录日志
     * 进度信息会通过日志输出
     */
    public AgentState executeAnalysisWithProgress(AgentState state, Consumer<ProgressUpdate> progressCallback) {
        // 由于当前Python服务器返回的是最终结果而非流式进度，
        // 我们使用同步执行并通过日志报告进度
        // 进度更新需要Python服务器支持SSE才能实现真正的实时推送
        
        log.info("Starting analysis with progress tracking for requestId: {}", state.getRequestId());
        
        // 模拟进度更新
        if (progressCallback != null) {
            progressCallback.accept(new ProgressUpdate("START", 5, "STARTING", state.getRequestId()));
        }
        
        // 执行分析
        AgentState result = executeAnalysis(state);
        
        // 报告完成
        if (progressCallback != null) {
            progressCallback.accept(new ProgressUpdate("COMPLETE", 100, "COMPLETED", state.getRequestId()));
        }
        
        return result;
    }
    
    /**
     * 获取执行器健康状态
     */
    public boolean isHealthy() {
        try {
            Request request = new Request.Builder()
                    .url(executorUrl + "/health")
                    .get()
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            log.warn("Executor health check failed", e);
            return false;
        }
    }
    
    /**
     * 进度更新类
     */
    @lombok.Data
    public static class ProgressUpdate {
        private String agent;
        private int progress;
        private String stage;
        private String requestId;
        
        public ProgressUpdate() {}
        
        public ProgressUpdate(String agent, int progress, String stage, String requestId) {
            this.agent = agent;
            this.progress = progress;
            this.stage = stage;
            this.requestId = requestId;
        }
    }
}
