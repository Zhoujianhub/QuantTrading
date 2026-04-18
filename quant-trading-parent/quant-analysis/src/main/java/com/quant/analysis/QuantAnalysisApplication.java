package com.quant.analysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 多Agent股票分析模块启动类
 */
@SpringBootApplication(scanBasePackages = "com.quant")
@EnableAsync
public class QuantAnalysisApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(QuantAnalysisApplication.class, args);
    }
}
