package com.github.yizzuide.milkomeda.metal;

import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * MetalHolder
 *
 * @author yizzuide
 * @since 3.6.0
 * @version 3.6.1
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
     * @param source    配置源
     * @since 3.6.1
     */
    public static void merge(Map<String, String> source) {
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
            updateProperty(key, value);
        }
    }

    /**
     * 本地更新配置项（在数据库配置更新成功后调用）
     * @param key   配置key
     * @param value 新值
     */
    public static void updateProperty(String key, String value) {
        synchronized(MetalHolder.class) {
            metalContainer.updateVNode(key, value);
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
