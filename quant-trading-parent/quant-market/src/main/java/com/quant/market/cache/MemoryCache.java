package com.quant.market.cache;

import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * L1内存缓存实现
 * 使用ConcurrentHashMap实现，支持TTL过期和LRU淘汰
 */
@Slf4j
public class MemoryCache {
    
    private final ConcurrentHashMap<String, CacheEntry> cache;
    private final long ttlMillis;
    private final int maxSize;
    private final ScheduledExecutorService cleaner;
    
    public MemoryCache(long ttlSeconds, int maxSize) {
        this.cache = new ConcurrentHashMap<>();
        this.ttlMillis = ttlSeconds * 1000L;
        this.maxSize = maxSize;
        this.cleaner = Executors.newSingleThreadScheduledExecutor();
        
        // 启动定时清理任务，每分钟检查一次过期数据
        this.cleaner.scheduleAtFixedRate(this::cleanExpired, 1, 1, TimeUnit.MINUTES);
    }
    
    /**
     * 存入缓存
     */
    public void put(String key, Serializable value) {
        if (key == null || value == null) {
            return;
        }
        
        // 超过最大容量时进行LRU淘汰
        if (cache.size() >= maxSize) {
            evictLRU();
        }
        
        CacheEntry entry = new CacheEntry(value, System.currentTimeMillis() + ttlMillis);
        cache.put(key, entry);
    }
    
    /**
     * 存入缓存，指定TTL
     */
    public void put(String key, Serializable value, long customTtlSeconds) {
        if (key == null || value == null) {
            return;
        }
        if (customTtlSeconds <= 0) {
            customTtlSeconds = ttlMillis / 1000;
        }
        CacheEntry entry = new CacheEntry(value, System.currentTimeMillis() + (customTtlSeconds * 1000L));
        cache.put(key, entry);
    }
    
    /**
     * 获取缓存
     */
    @SuppressWarnings("unchecked")
    public <T extends Serializable> T get(String key) {
        if (key == null) {
            return null;
        }
        
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        
        // 检查是否过期
        if (System.currentTimeMillis() > entry.expireTime) {
            cache.remove(key);
            return null;
        }
        
        // 更新访问时间（用于LRU）
        entry.accessTime = System.currentTimeMillis();
        return (T) entry.value;
    }
    
    /**
     * 检查键是否存在且未过期
     */
    public boolean contains(String key) {
        if (key == null) {
            return false;
        }
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return false;
        }
        if (System.currentTimeMillis() > entry.expireTime) {
            cache.remove(key);
            return false;
        }
        return true;
    }
    
    /**
     * 删除缓存
     */
    public void remove(String key) {
        if (key != null) {
            cache.remove(key);
        }
    }
    
    /**
     * 清空所有缓存
     */
    public void clear() {
        cache.clear();
    }
    
    /**
     * 获取缓存条目数
     */
    public int size() {
        return cache.size();
    }
    
    /**
     * 关闭缓存清理线程
     */
    public void shutdown() {
        cleaner.shutdown();
        try {
            if (!cleaner.awaitTermination(5, TimeUnit.SECONDS)) {
                cleaner.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleaner.shutdownNow();
        }
    }
    
    /**
     * 清理过期数据
     */
    private void cleanExpired() {
        long now = System.currentTimeMillis();
        int removed = 0;
        Iterator<Map.Entry<String, CacheEntry>> iterator = cache.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, CacheEntry> entry = iterator.next();
            if (now > entry.getValue().expireTime) {
                iterator.remove();
                removed++;
            }
        }
        if (removed > 0) {
            log.debug("清理过期缓存条目: {}个", removed);
        }
    }
    
    /**
     * LRU淘汰：移除最久未访问的条目
     */
    private void evictLRU() {
        if (cache.isEmpty()) {
            return;
        }
        
        String lruKey = null;
        long oldestAccess = Long.MAX_VALUE;
        
        for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
            if (entry.getValue().accessTime < oldestAccess) {
                oldestAccess = entry.getValue().accessTime;
                lruKey = entry.getKey();
            }
        }
        
        if (lruKey != null) {
            cache.remove(lruKey);
            log.debug("LRU淘汰缓存键: {}", lruKey);
        }
    }
    
    /**
     * 缓存条目内部类
     */
    private static class CacheEntry {
        Serializable value;
        long expireTime;
        long accessTime;
        
        CacheEntry(Serializable value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
            this.accessTime = System.currentTimeMillis();
        }
    }
}