package com.github.yizzuide.milkomeda.metal;

import java.util.Map;

/**
 * MetalHolder
 *
 * @author yizzuide
 * @since 3.6.0
 * Create at 2020/05/21 23:19
 */
public class MetalHolder {
    /**
     * 容器
     */
    private static MetalContainer metalContainer;

    static void setMetalContainer(MetalContainer metalContainer) {
        MetalHolder.metalContainer = metalContainer;
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
     * 更新配置项（在数据库配置更新成功后调用）
     * @param key   配置key
     * @param value 新值
     */
    public static synchronized void updateProperty(String key, String value) {
        metalContainer.updateVNode(key, value);
    }
}
