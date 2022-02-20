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

package com.github.yizzuide.milkomeda.sundial;

import com.github.yizzuide.milkomeda.util.DataTypeConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertyNameAliases;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Map;


/**
 * 动态创建数据源工厂
 * @author jsq 786063250@qq.com
 * @since 3.4.0
 * Create at 2020/5/8
 */
@Slf4j
public class DataSourceFactory implements EnvironmentAware {

    @Resource
    private SundialProperties sundialProperties;

    public static String DEFAULT_DATASOURCE_PREFIX = "spring.datasource";

    private static final ConfigurationPropertyNameAliases ALIASES = new ConfigurationPropertyNameAliases();

    /**
     * 配置绑定
     */
    private Binder binder;

    @PostConstruct
    public void init() {
        // 设置数据源模板前缀
        DEFAULT_DATASOURCE_PREFIX = sundialProperties.getConfigPrefix();
        // DataSource匿名属性匹配
        ALIASES.addAliases("url", "jdbc-url");
        ALIASES.addAliases("username", "user");
    }

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        binder = Binder.get(environment);
    }

    /**
     * 创建用户配置的数据源
     * @return DataSource
     * @throws Exception    创建异常
     */
    public DataSource createDataSource() throws Exception {
        return createDataSource(null);
    }

    /**
     * 创建数据源实例
     * @param dataSourceConf    数据源配置
     * @return DataSource
     * @throws Exception    创建异常
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public DataSource createDataSource(SundialProperties.Datasource dataSourceConf) throws Exception {
        Map dataSourceProperties = binder.bind(DEFAULT_DATASOURCE_PREFIX, Map.class).get();
        // 新的配置项覆盖spring.datasource原有配置来创建新的DataSource
        if (dataSourceConf != null) {
            Map<String, Object> dataSourceConfMap = DataTypeConvertUtil.beanToMap(dataSourceConf);
            dataSourceProperties.putAll(dataSourceConfMap);
        }
        // 根据数据源类型创建实例
        DataSource dataSource = sundialProperties.getDatasourceType().newInstance();
        // 绑定数据源配置值
        bind(dataSource, dataSourceProperties);
        return dataSource;
    }

    @SuppressWarnings("rawtypes")
    private void bind(DataSource result, Map properties) {
        ConfigurationPropertySource source = new MapConfigurationPropertySource(properties);
        // 创建绑定对象，拷贝并添加匿名key的值
        Binder binder = new Binder(source.withAliases(ALIASES));
        binder.bind(ConfigurationPropertyName.EMPTY, Bindable.ofInstance(result));
    }
}
