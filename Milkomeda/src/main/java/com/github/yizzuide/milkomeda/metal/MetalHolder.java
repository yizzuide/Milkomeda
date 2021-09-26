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
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * MetalHolder
 *
 * @author yizzuide
 * @since 3.6.0
 * @version 3.6.2
 * Create at 2020/05/21 23:19
 */
public class MetalHolder {

    /**
     * 容器
     */
    private static MetalContainer metalContainer;

    /**
     * 分布式配置同步消息处理器
     */
    private static MetalMessageHandler metalMessageHandler;

    static void setMetalContainer(MetalContainer metalContainer) {
        MetalHolder.metalContainer = metalContainer;
    }

    static void setMetalMessageHandler(MetalMessageHandler metalMessageHandler) {
        MetalHolder.metalMessageHandler = metalMessageHandler;
    }

    /**
     * 初始化配置源（该方法应该在应用启动完成时调用，因为线程不安全）
     * @param source 配置源
     */
    public static void init(Map<String, String> source) {
        metalContainer.init(source);
    }

    /**
     * 获取配置值
     * @param key   配置key
     * @return  值
     */
    public static String getProperty(String key) {
        return metalContainer.getProperty(key);
    }


    /**
     * 合并配置源
     * @param source        配置源
     * @since 3.6.1
     */
    public static void merge(Map<String, String> source) {
        merge(source, false);
    }

    /**
     * 合并配置源
     * @param source        配置源
     * @param syncRemote    远程同步更新
     * @since 3.6.2
     */
    public static void merge(Map<String, String> source, boolean syncRemote) {
        if (CollectionUtils.isEmpty(source)) {
            return;
        }
        for (Map.Entry<String, String> entry : source.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String oldVal = getProperty(key);
            // 相同的不更新
            if (oldVal != null && oldVal.equals(value)) {
                continue;
            }
            if (syncRemote) {
                remoteUpdateProperty(key, value);
                continue;
            }
            updateProperty(key, value);
        }
    }

    /**
     * 本地更新配置项（在数据库配置更新成功后调用）
     * @param key   配置key
     * @param value 新值
     */
    public static void updateProperty(String key, String value) {
        String oldVal = metalContainer.getProperty(key);
        if (oldVal != null && !oldVal.equals(value)) {
            ApplicationContextHolder.get().publishEvent(new MetalChangeEvent(metalContainer.getSource(), key, oldVal, value));
        }
        synchronized(MetalHolder.class) {
            metalContainer.updateVNode(key, oldVal, value);
        }
    }

    /**
     * 分布式远程更新配置项（在数据库配置更新成功后调用）
     * @param key   配置key
     * @param value 新值
     */
    public static void remoteUpdateProperty(String key, String value) {
        if (metalMessageHandler != null) {
            metalMessageHandler.buildAndSendMessage(key, value);
        }
    }
}
