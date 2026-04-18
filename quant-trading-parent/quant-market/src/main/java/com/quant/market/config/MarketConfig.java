package com.quant.market.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 市场模块配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "market")
public class MarketConfig {
    
    /** Tushare Pro API Token */
    private String tushareToken;
    
    /** Tushare API地址 */
    private String tushareUrl = "http://api.tushare.pro";
    
    /** 东方财富API地址 */
    private String eastMoneyUrl = "https://push2.eastmoney.com";
    
    /** L1内存缓存TTL（秒），默认60秒 */
    private long memoryCacheTtl = 60;
    
    /** L2文件缓存TTL（秒），默认3600秒（1小时） */
    private long fileCacheTtl = 3600;
    
    /** 内存缓存最大条目数 */
    private int memoryCacheMaxSize = 1000;
    
    /** 文件缓存目录 */
    private String fileCacheDir = "./cache/market";
    
    /** 是否启用缓存 */
    private boolean cacheEnabled = true;
    
    /** 数据源优先级：tushare,eastmoney */
    private String dataSourcePriority = "tushare,eastmoney";
    
    /** HTTP请求超时时间（毫秒） */
    private int httpTimeout = 10000;
    
    /** 是否启用模拟数据（当所有数据源都不可用时） */
    private boolean mockDataEnabled = true;
}