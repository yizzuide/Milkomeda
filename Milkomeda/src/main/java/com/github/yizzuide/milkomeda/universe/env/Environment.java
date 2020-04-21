package com.github.yizzuide.milkomeda.universe.env;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Properties;

/**
 * Environment
 *
 * @author yizzuide
 * @since 3.1.0
 * Create at 2020/04/21 10:44
 */
public class Environment {

    // Spring可配置环境变量
    private ConfigurableEnvironment configurableEnvironment;

    // 附加配置源
    private Properties properties = new Properties();

    public void setConfigurableEnvironment(ConfigurableEnvironment configurableEnvironment) {
        this.configurableEnvironment = configurableEnvironment;
        MutablePropertySources propertySources = configurableEnvironment.getPropertySources();
        propertySources.addLast(new PropertiesPropertySource("milkomedaProperties", properties));
    }

    /**
     * 添加Spring环境变量
     * @param key   键
     * @param value 值
     */
    public void put(String key, String value) {
        this.properties.setProperty(key, value);
    }

    /**
     * 获取Spring环境变量
     * @param key   键
     * @return  如果key不存在，返回null
     */
    public @Nullable String get(String key) {
        if (this.configurableEnvironment == null) {
            return null;
        }
        return this.configurableEnvironment.getProperty(key);
    }

    /**
     * 获取配置源
     * @return  Properties
     */
    public @NonNull Properties getProperties() {
        return this.properties;
    }
}
