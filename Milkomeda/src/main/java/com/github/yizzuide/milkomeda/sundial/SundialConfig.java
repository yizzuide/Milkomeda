package com.github.yizzuide.milkomeda.sundial;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * 自定义数据源配置
 * @date 2020/5/8
 * @author jsq 786063250@qq.com
 * @since 3.4.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(SundialProperties.class)
@AutoConfigureBefore(TransactionAutoConfiguration.class)
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

    @Bean
    public DynamicRouteDataSource dynamicRouteDataSource(DataSourceFactory dataSourceFactory) {
        DataSource defaultDataSource = null;
        Map<Object, Object> targetDataSources = new HashMap<>();
        try {
            defaultDataSource = dataSourceFactory.createDataSource();
            targetDataSources.put(DynamicRouteDataSource.MASTER_KEY, defaultDataSource);
            Set<Map.Entry<String, SundialProperties.Datasource>> datasourceConfigSet = props.getInstances().entrySet();
            if (CollectionUtils.isEmpty(datasourceConfigSet)) {
                return new DynamicRouteDataSource(defaultDataSource, targetDataSources);
            }
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
