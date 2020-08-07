package com.github.yizzuide.milkomeda.universe.env;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * CustomPropertySourceLocator
 * 自定义属性源
 *
 * @author yizzuide
 * @since 3.0.1
 * @version 3.2.1
 * Create at 2020/04/11 10:48
 */
public class CustomPropertySourceLocator implements /*PropertySourceLocator,*/ EnvironmentPostProcessor {

    // 添加自定义属性来源方式一：实现PropertySourceLocator接口，把CollectionsPropertySourceLocator注册为Bean
    /*@Override
    public PropertySource<?> locate(Environment environment) {
        Map<String, Object> mapResource = new HashMap<>();
        mapResource.put("field", "value");
        return new MapPropertySource("milkomeda", mapResource);
    }*/

    // 添加自定义属性来源方式二：实现EnvironmentPostProcessor接口，把CollectionsPropertySourceLocator注册到spring.factories
    // Spring启动时发出ApplicationEnvironmentPreparedEvent事件，通过ConfigFileApplicationListener加载SPI配置的所有排好序的EnvironmentPostProcessor实例
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        CollectionsPropertySource.addToEnvironment(environment);
        ConditionPropertySource.addToEnvironment(environment);
    }
}
