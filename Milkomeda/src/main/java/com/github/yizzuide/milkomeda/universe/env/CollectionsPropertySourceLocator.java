package com.github.yizzuide.milkomeda.universe.env;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * MilkomedaPropertySourceLocator
 *
 * @author yizzuide
 * @since 3.0.1
 * Create at 2020/04/11 10:48
 */
public class CollectionsPropertySourceLocator implements /*PropertySourceLocator,*/ EnvironmentPostProcessor {

    // 添加自定义属性来源方式一：实现PropertySourceLocator接口，把CollectionsPropertySourceLocator注册为Bean
    /*@Override
    public PropertySource<?> locate(Environment environment) {
        Map<String, Object> mapResource = new HashMap<>();
        mapResource.put("field", "value");
        return new MapPropertySource("milkomeda", mapResource);
    }*/

    // 添加自定义属性来源方式二：实现EnvironmentPostProcessor接口，把CollectionsPropertySourceLocator注册到spring.factories
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        CollectionsPropertySource.addToEnvironment(environment);
    }
}
