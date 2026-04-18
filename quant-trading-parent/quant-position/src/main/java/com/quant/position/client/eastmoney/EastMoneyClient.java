package com.quant.position.client.eastmoney;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.quant.common.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class EastMoneyClient {

    private static final String REALTIME_NAV_URL = "http://fundgz.1234567.com.cn/js/{assetCode}.js";
    private static final Gson GSON = new Gson();
    private static final Pattern JSONP_PATTERN = Pattern.compile("\\((.+)\\)");

    public BigDecimal getRealTimeNav(String assetCode) {
        try {
            String url = REALTIME_NAV_URL.replace("{assetCode}", assetCode);
            log.info("调用天天基金API: url={}", url);
            String response = HttpUtils.get(url);

            if (response == null || response.isEmpty()) {
                log.warn("获取基金净值返回为空: assetCode={}", assetCode);
                return null;
            }

            log.info("天天基金API响应: assetCode={}, response={}", assetCode, response);

            Matcher matcher = JSONP_PATTERN.matcher(response);
            if (matcher.find()) {
                String jsonStr = matcher.group(1);
                JsonObject jsonObject = GSON.fromJson(jsonStr, JsonObject.class);

                String rtCode = jsonObject.has("rt") ? jsonObject.get("rt").getAsString() : null;
                log.info("解析JSON - rt状态码: {}, assetCode={}", rtCode, assetCode);

                if ("0".equals(rtCode)) {
                    if (jsonObject.has("dwjz") && !jsonObject.get("dwjz").isJsonNull()) {
                        String navStr = jsonObject.get("dwjz").getAsString();
                        log.info("解析到净值: assetCode={}, nav={}", assetCode, navStr);
                        return new BigDecimal(navStr);
                    } else {
                        log.warn("净值字段dwjz为空或不存在: assetCode={}", assetCode);
                    }
                } else {
                    String msg = jsonObject.has("errMsg") ? jsonObject.get("errMsg").getAsString() : "未知错误";
                    log.warn("API返回错误: assetCode={}, rt={}, errMsg={}", assetCode, rtCode, msg);
                }
            } else {
                log.warn("JSONP解析失败，无法匹配到JSON: assetCode={}, response={}", assetCode, response);
            }
            return null;
        } catch (Exception e) {
            log.error("获取基金净值异常: assetCode={}", assetCode, e);
            return null;
        }
    }
}
