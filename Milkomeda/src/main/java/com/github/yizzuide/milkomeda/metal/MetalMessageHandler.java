package com.github.yizzuide.milkomeda.metal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

/**
 * MetalMessageHandler
 * 分布式配置同步消息处理器
 *
 * @author yizzuide
 * @since 3.6.0
 * Create at 2020/05/22 15:59
 */
public class MetalMessageHandler {
    public static final String METAL_CHANGE_TOPIC = "MK_METAL_TOPIC";
    public static final String METAL_MSG_KV_SEPARATOR= " -> ";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void handleMessage(String message) {
        String[] kv =  StringUtils.delimitedListToStringArray(message, METAL_MSG_KV_SEPARATOR);
        String key = kv[0];
        String value = kv[1];
        MetalHolder.updateProperty(key, value);
    }

    /**
     * 发布配置修改消息
     * @param key   配置key
     * @param value 值
     */
    public void buildAndSendMessage(String key,  String value) {
        stringRedisTemplate.convertAndSend(METAL_CHANGE_TOPIC, key + METAL_MSG_KV_SEPARATOR + value);
    }
}
