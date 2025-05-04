/*
 * Copyright (c) 2025 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.light;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

/**
 * 缓存Key修改消息处理器
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/05/02 17:09
 */
public class LightMessageHandler {

    public static String LIGHT_CHANGE_TOPIC;

    public static final String LIGHT_MSG_SEPARATOR = ":";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    // 这个方法会在接受到消息时自动调用
    public void handleMessage(String message) {
        String[] items =  StringUtils.delimitedListToStringArray(message, LIGHT_MSG_SEPARATOR);
        String cacheName = items[0];
        String key = items[1];
        Cache cache = ApplicationContextHolder.get().getBean(cacheName, LightCache.class);
        cache.eraseL1(key);
    }


    /**
     * 发布Key修改消息
     * @param cacheName   缓存名
     * @param key 缓存 key
     */
    public void buildAndSendMessage(String cacheName, String key) {
        stringRedisTemplate.convertAndSend(LIGHT_CHANGE_TOPIC, cacheName + LIGHT_MSG_SEPARATOR + key);
    }

    /**
     * 获取Topic名
     * @param applicationName 应用服务名
     * @return  topic
     */
    public static String getTopic(String applicationName) {
        if (LIGHT_CHANGE_TOPIC != null) {
            return LIGHT_CHANGE_TOPIC;
        }
        LIGHT_CHANGE_TOPIC = RedisUtil.buildChannelName(applicationName, "MK_LIGHT_TOPIC", "MK_LIGHT_%s_TOPIC");
        return LIGHT_CHANGE_TOPIC;
    }
}
