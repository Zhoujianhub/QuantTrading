package com.quant.common.util;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;

import java.util.Map;

public class HttpUtils {

    private static final Gson GSON = new Gson();

    private HttpUtils() {
    }

    public static String get(String url) {
        return HttpUtil.get(url);
    }

    public static String get(String url, Map<String, ?> params) {
        return HttpUtil.get(url, (Map<String, Object>) params, 3000);
    }

    public static String post(String url, String body) {
        return HttpUtil.post(url, body);
    }

    public static <T> T get(String url, Class<T> clazz) {
        String result = get(url);
        return GSON.fromJson(result, clazz);
    }

    public static <T> T post(String url, String body, Class<T> clazz) {
        String result = post(url, body);
        return GSON.fromJson(result, clazz);
    }
}
