package com.github.yizzuide.milkomeda.sundial;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @date: 2020/5/8
 * @author: jsq
 * @email: 786063250@qq.com
 * @describe:
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(SundialProperties.class)
@AutoConfigureAfter(TransactionAutoConfiguration.class)
public class SundialConfig implements ApplicationContextAware {

    @Autowired
    private SundialProperties props;

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

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {

    }

    /*@EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshedEvent() {
        DataSourceFactory dataSourceFactory = ApplicationContextHolder.get().getBean(DataSourceFactory.class);
        DynamicRouteDataSource dynamicRouteDataSource = ApplicationContextHolder.get().getBean(DynamicRouteDataSource.class);
        try {
            DataSource dataSource = dataSourceFactory.createDataSource();
            dynamicRouteDataSource.put(DynamicRouteDataSource.MASTER_KEY, dataSource, true);
            Set<Map.Entry<String, SundialProperties.Datasource>> datasourceConfigSet = props.getInstances().entrySet();
            if (CollectionUtils.isEmpty(datasourceConfigSet)) {
                return;
            }
            for (Map.Entry<String, SundialProperties.Datasource> entry : datasourceConfigSet) {
                dataSource = dataSourceFactory.createDataSource(entry.getValue());
                dynamicRouteDataSource.put(entry.getKey(), dataSource, false);
            }
        } catch (Exception e) {
            log.error("Sundial load datasource instances error with msg: {}", e.getMessage(), e);
        }
    }*/
}
