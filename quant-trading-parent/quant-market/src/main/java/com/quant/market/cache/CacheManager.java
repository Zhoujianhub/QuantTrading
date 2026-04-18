package com.quant.market.cache;

import com.quant.market.config.MarketConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 多层缓存管理器
 * 统一管理L1内存缓存和L2文件缓存，实现缓存读取回填机制
 */
@Slf4j
@Component
public class CacheManager {
    
    private MemoryCache l1Cache;
    private FileCache l2Cache;
    
    @Autowired
    private MarketConfig marketConfig;
    
    private static final String KEY_PREFIX_REALTIME = "realtime:";
    private static final String KEY_PREFIX_KLINE = "kline:";
    private static final String KEY_PREFIX_INDICATORS = "indicators:";
    
    @PostConstruct
    public void init() {
        if (!marketConfig.isCacheEnabled()) {
            log.info("缓存功能已禁用");
            return;
        }
        
        this.l1Cache = new MemoryCache(marketConfig.getMemoryCacheTtl(), marketConfig.getMemoryCacheMaxSize());
        this.l2Cache = new FileCache(marketConfig.getFileCacheDir(), marketConfig.getFileCacheTtl());
        
        log.info("缓存管理器初始化完成: L1={}s/{}条, L2={}s, 路径={}",
                marketConfig.getMemoryCacheTtl(),
                marketConfig.getMemoryCacheMaxSize(),
                marketConfig.getFileCacheTtl(),
                marketConfig.getFileCacheDir());
    }
    
    @PreDestroy
    public void destroy() {
        if (l1Cache != null) {
            l1Cache.shutdown();
        }
        log.info("缓存管理器已关闭");
    }
    
    /**
     * 获取实时行情（先查L1，再查L2，最后回填）
     */
    public <T extends Serializable> T getRealtimeQuote(String stockCode, Class<T> clazz) {
        String key = KEY_PREFIX_REALTIME + stockCode;
        return get(key, clazz);
    }
    
    /**
     * 存入实时行情
     */
    public <T extends Serializable> void putRealtimeQuote(String stockCode, T value) {
        String key = KEY_PREFIX_REALTIME + stockCode;
        put(key, value);
    }
    
    /**
     * 获取历史K线
     */
    public <T extends Serializable> T getHistoricalKline(String stockCode, String period, Class<T> clazz) {
        String key = KEY_PREFIX_KLINE + stockCode + ":" + period;
        return get(key, clazz);
    }
    
    /**
     * 存入历史K线
     */
    public <T extends Serializable> void putHistoricalKline(String stockCode, String period, T value) {
        String key = KEY_PREFIX_KLINE + stockCode + ":" + period;
        put(key, value);
    }
    
    /**
     * 获取技术指标
     */
    public <T extends Serializable> T getTechnicalIndicators(String stockCode, Class<T> clazz) {
        String key = KEY_PREFIX_INDICATORS + stockCode;
        return get(key, clazz);
    }
    
    /**
     * 存入技术指标
     */
    public <T extends Serializable> void putTechnicalIndicators(String stockCode, T value) {
        String key = KEY_PREFIX_INDICATORS + stockCode;
        put(key, value);
    }
    
    /**
     * 通用获取：先L1后L2，回填机制
     */
    @SuppressWarnings("unchecked")
    public <T extends Serializable> T get(String key, Class<T> clazz) {
        if (!marketConfig.isCacheEnabled()) {
            return null;
        }
        
        // 先查L1内存缓存
        if (l1Cache != null) {
            T value = l1Cache.get(key);
            if (value != null) {
                log.debug("缓存命中(L1): key={}", key);
                return value;
            }
        }
        
        // L1未命中，查L2文件缓存
        if (l2Cache != null) {
            try {
                T value = l2Cache.get(key, clazz);
                if (value != null) {
                    log.debug("缓存命中(L2): key={}", key);
                    // 回填到L1
                    if (l1Cache != null) {
                        l1Cache.put(key, value);
                    }
                    return value;
                }
            } catch (Exception e) {
                log.warn("L2缓存读取失败: key={}", key, e);
            }
        }
        
        log.debug("缓存未命中: key={}", key);
        return null;
    }
    
    /**
     * 通用存入：同时写入L1和L2
     */
    public <T extends Serializable> void put(String key, T value) {
        if (!marketConfig.isCacheEnabled() || value == null) {
            return;
        }
        
        // 写入L1内存缓存
        if (l1Cache != null) {
            l1Cache.put(key, value);
        }
        
        // 写入L2文件缓存
        if (l2Cache != null) {
            try {
                l2Cache.put(key, value);
            } catch (Exception e) {
                log.warn("L2缓存写入失败: key={}", key, e);
            }
        }
        
        log.debug("缓存写入: key={}", key);
    }
    
    /**
     * 存入缓存，指定TTL
     */
    public <T extends Serializable> void put(String key, T value, long ttlSeconds) {
        if (!marketConfig.isCacheEnabled() || value == null) {
            return;
        }
        
        if (l1Cache != null) {
            l1Cache.put(key, value, ttlSeconds);
        }
        if (l2Cache != null) {
            try {
                l2Cache.put(key, value, ttlSeconds);
            } catch (Exception e) {
                log.warn("L2缓存写入失败: key={}", key, e);
            }
        }
    }
    
    /**
     * 删除缓存
     */
    public void evict(String key) {
        if (l1Cache != null) {
            l1Cache.remove(key);
        }
        if (l2Cache != null) {
            l2Cache.remove(key);
        }
        log.debug("缓存删除: key={}", key);
    }
    
    /**
     * 清空所有缓存
     */
    public void clear() {
        if (l1Cache != null) {
            l1Cache.clear();
        }
        if (l2Cache != null) {
            l2Cache.clear();
        }
        log.info("缓存已清空");
    }
    
    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        if (l1Cache != null) {
            stats.put("l1Size", l1Cache.size());
        }
        if (l2Cache != null) {
            stats.put("l2Size", l2Cache.size());
        }
        stats.put("cacheEnabled", marketConfig.isCacheEnabled());
        return stats;
    }
}