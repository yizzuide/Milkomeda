/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.metal;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

/**
 * MetalMessageHandler
 * 分布式配置同步消息处理器
 *
 * @author yizzuide
 * @since 3.6.0
 * @version 3.6.1
 * Create at 2020/05/22 15:59
 */
public class MetalMessageHandler {

    private static String METAL_CHANGE_TOPIC;

    private static final String METAL_MSG_KV_SEPARATOR= " -> ";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 这个方法会在接受到消息时自动调用
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
    public void buildAndSendMessage(String key, String value) {
        stringRedisTemplate.convertAndSend(METAL_CHANGE_TOPIC, key + METAL_MSG_KV_SEPARATOR + value);
    }

    /**
     * 获取Topic名
     * @param applicationName 应用服务名
     * @return  topic
     */
    public static String getTopic(String applicationName) {
        if (METAL_CHANGE_TOPIC != null) {
            return METAL_CHANGE_TOPIC;
        }
        if (applicationName == null) {
            String appName = ApplicationContextHolder.getEnvironment().get("spring.application.name");
            if (Strings.isEmpty(appName)) {
                METAL_CHANGE_TOPIC = "MK_METAL_TOPIC";
                return METAL_CHANGE_TOPIC;
            }
            METAL_CHANGE_TOPIC = String.format("MK_METAL_%s_TOPIC", appName);
            return METAL_CHANGE_TOPIC;
        }
        METAL_CHANGE_TOPIC = String.format("MK_METAL_%s_TOPIC", applicationName);
        return METAL_CHANGE_TOPIC;
    }
}
