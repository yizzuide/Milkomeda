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

import com.github.yizzuide.milkomeda.orbit.OrbitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * 自定义数据源配置
 * @author jsq 786063250@qq.com
 * @since 3.4.0
 * @version 3.13.10
 * Create at 2020/5/8
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@Import(OrbitConfig.class)
@EnableConfigurationProperties(SundialProperties.class)
@AutoConfigureBefore({DataSourceAutoConfiguration.class, TransactionAutoConfiguration.class})
public class SundialConfig {

    @Autowired
    private SundialProperties props;

    @Bean
    public DataSourceAspect dataSourceAspect(){
        return new DataSourceAspect();
    }

    @Bean
    public DataSourceFactory dataSourceFactory(){
        return new DataSourceFactory();
    }

    @Bean(ShardingFunction.BEAN_ID)
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = SundialProperties.PREFIX, name = "enable-sharding", havingValue = "true")
    public ShardingFunction shardingFunction() {
        return new ShardingFunction();
    }

    @Bean
    @ConditionalOnProperty(prefix = SundialProperties.PREFIX, name = "enable-sharding", havingValue = "true")
    public SundialInterceptor sundialInterceptor() {
        return new SundialInterceptor();
    }

    @Primary
    @Bean
    public DataSource dynamicRouteDataSource(DataSourceFactory dataSourceFactory) {
        DataSource defaultDataSource = null;
        Map<Object, Object> targetDataSources = new HashMap<>();
        try {
            defaultDataSource = dataSourceFactory.createDataSource();
            // 添加主数据源
            targetDataSources.put(DynamicRouteDataSource.MASTER_KEY, defaultDataSource);
            Set<Map.Entry<String, SundialProperties.Datasource>> datasourceConfigSet = props.getInstances().entrySet();
            if (CollectionUtils.isEmpty(datasourceConfigSet)) {
                return new DynamicRouteDataSource(defaultDataSource, targetDataSources);
            }
            // 添加其它数据源节点
            for (Map.Entry<String, SundialProperties.Datasource> entry : datasourceConfigSet) {
                DataSource dataSource = dataSourceFactory.createDataSource(entry.getValue());
                targetDataSources.put(entry.getKey(), dataSource);
            }
        } catch (Exception e) {
            log.error("Sundial load datasource instances error with msg: {}", e.getMessage(), e);
        }
        return new DynamicRouteDataSource(defaultDataSource, targetDataSources);
    }
}
