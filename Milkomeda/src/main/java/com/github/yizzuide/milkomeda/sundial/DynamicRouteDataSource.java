package com.github.yizzuide.milkomeda.sundial;

import lombok.NoArgsConstructor;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;



/**
 * 数据源路由
 * @date 2020/5/8
 * @author jsq 786063250@qq.com
 * @since 3.4.0
 */
@NoArgsConstructor
public class DynamicRouteDataSource extends AbstractRoutingDataSource {

    public static final String MASTER_KEY = "master";

    private Map<Object, Object> wrapperDataSources = new HashMap<>();

    public DynamicRouteDataSource(DataSource defaultTargetDataSource, Map<Object, Object> targetDataSources)
    {
        setDataSources(defaultTargetDataSource, targetDataSources);
        this.wrapperDataSources = targetDataSources;
    }

    @Override
    protected Object determineCurrentLookupKey()
    {
        return DynamicDataSourceContextHolder.getDataSourceType();
    }

    /**
     * 添加数据源到动态路由
     * @param routeKey      路由key
     * @param dataSource    数据源
     * @param isDefault     是否设置为默认
     */
    public void put(Object routeKey, DataSource dataSource, boolean isDefault) {
        wrapperDataSources.put(routeKey, dataSource);
        setDataSources(isDefault ? dataSource : null, wrapperDataSources);
    }

    private void setDataSources(DataSource defaultTargetDataSource, Map<Object, Object> targetDataSources) {
        if (defaultTargetDataSource != null) {
            this.setDefaultTargetDataSource(defaultTargetDataSource);
        }
        this.setTargetDataSources(targetDataSources);
        this.afterPropertiesSet();
    }
}