package com.quant.market.cache;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * L2文件缓存实现
 * 使用GZIP压缩实现文件缓存，支持TTL过期
 */
@Slf4j
public class FileCache {
    
    private final String cacheDir;
    private final long ttlMillis;
    private final Gson gson;
    
    public FileCache(String cacheDir, long ttlSeconds) {
        this.cacheDir = cacheDir;
        this.ttlMillis = ttlSeconds * 1000L;
        this.gson = new Gson();
        
        // 确保缓存目录存在
        File dir = new File(cacheDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    /**
     * 存入缓存
     */
    public void put(String key, Object value) throws IOException {
        if (key == null || value == null) {
            return;
        }
        
        String fileName = getFileName(key);
        File file = new File(cacheDir, fileName);
        
        CacheData cacheData = new CacheData();
        cacheData.key = key;
        cacheData.value = gson.toJson(value);
        cacheData.expireTime = System.currentTimeMillis() + ttlMillis;
        cacheData.createdTime = System.currentTimeMillis();
        
        try (FileOutputStream fos = new FileOutputStream(file);
             java.util.zip.GZIPOutputStream gzos = new java.util.zip.GZIPOutputStream(fos);
             OutputStreamWriter writer = new OutputStreamWriter(gzos)) {
            gson.toJson(cacheData, writer);
        }
        
        log.debug("文件缓存写入: key={}, file={}", key, fileName);
    }
    
    /**
     * 存入缓存，指定TTL
     */
    public void put(String key, Object value, long customTtlSeconds) throws IOException {
        if (key == null || value == null) {
            return;
        }
        
        String fileName = getFileName(key);
        File file = new File(cacheDir, fileName);
        
        CacheData cacheData = new CacheData();
        cacheData.key = key;
        cacheData.value = gson.toJson(value);
        cacheData.expireTime = System.currentTimeMillis() + (customTtlSeconds * 1000L);
        cacheData.createdTime = System.currentTimeMillis();
        
        try (FileOutputStream fos = new FileOutputStream(file);
             java.util.zip.GZIPOutputStream gzos = new java.util.zip.GZIPOutputStream(fos);
             OutputStreamWriter writer = new OutputStreamWriter(gzos)) {
            gson.toJson(cacheData, writer);
        }
    }
    
    /**
     * 获取缓存
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        if (key == null) {
            return null;
        }
        
        String fileName = getFileName(key);
        File file = new File(cacheDir, fileName);
        
        if (!file.exists()) {
            return null;
        }
        
        // 检查是否过期
        if (System.currentTimeMillis() > file.lastModified() + ttlMillis) {
            file.delete();
            return null;
        }
        
        try (FileInputStream fis = new FileInputStream(file);
             java.util.zip.GZIPInputStream gzis = new java.util.zip.GZIPInputStream(fis);
             InputStreamReader reader = new InputStreamReader(gzis)) {
            
            CacheData cacheData = gson.fromJson(reader, CacheData.class);
            
            if (cacheData == null || System.currentTimeMillis() > cacheData.expireTime) {
                if (cacheData != null) {
                    file.delete();
                }
                return null;
            }
            
            return gson.fromJson(cacheData.value, clazz);
        } catch (IOException e) {
            log.warn("读取文件缓存失败: key={}", key, e);
            file.delete();
            return null;
        }
    }
    
    /**
     * 获取缓存，返回JsonObject类型
     */
    public JsonObject getAsJsonObject(String key) {
        return get(key, JsonObject.class);
    }
    
    /**
     * 检查键是否存在且未过期
     */
    public boolean contains(String key) {
        if (key == null) {
            return false;
        }
        
        String fileName = getFileName(key);
        File file = new File(cacheDir, fileName);
        
        if (!file.exists()) {
            return false;
        }
        
        // 检查文件修改时间判断TTL
        if (System.currentTimeMillis() > file.lastModified() + ttlMillis) {
            file.delete();
            return false;
        }
        
        return true;
    }
    
    /**
     * 删除缓存
     */
    public boolean remove(String key) {
        if (key == null) {
            return false;
        }
        
        String fileName = getFileName(key);
        File file = new File(cacheDir, fileName);
        return file.exists() && file.delete();
    }
    
    /**
     * 清空所有缓存
     */
    public void clear() {
        File dir = new File(cacheDir);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        }
    }
    
    /**
     * 获取缓存文件数量
     */
    public int size() {
        File dir = new File(cacheDir);
        if (!dir.exists() || !dir.isDirectory()) {
            return 0;
        }
        return Objects.requireNonNull(dir.listFiles()).length;
    }
    
    /**
     * 清理过期缓存文件
     */
    public void cleanExpired() {
        File dir = new File(cacheDir);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        
        long now = System.currentTimeMillis();
        int removed = 0;
        
        for (File file : files) {
            // 根据文件名判断是否过期（简化处理，实际应该读取文件内容检查expireTime）
            if (now > file.lastModified() + ttlMillis) {
                if (file.delete()) {
                    removed++;
                }
            }
        }
        
        if (removed > 0) {
            log.debug("清理过期文件缓存: {}个", removed);
        }
    }
    
    /**
     * 根据key生成文件名
     */
    private String getFileName(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(key.getBytes());
            String hash = Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
            return hash + ".cache";
        } catch (NoSuchAlgorithmException e) {
            // MD5总是可用，如果失败使用hashCode
            return Integer.toHexString(key.hashCode()) + ".cache";
        }
    }
    
    /**
     * 缓存数据结构
     */
    private static class CacheData implements Serializable {
        private static final long serialVersionUID = 1L;
        String key;
        String value;
        long expireTime;
        long createdTime;
    }
}