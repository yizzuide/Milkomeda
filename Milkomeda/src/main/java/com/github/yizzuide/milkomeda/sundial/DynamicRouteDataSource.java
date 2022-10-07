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

import lombok.NoArgsConstructor;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;



/**
 * 数据源路由
 * @author jsq 786063250@qq.com
 * @since 3.4.0
 * <br />
 * Create at 2020/5/8
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
    protected Object determineCurrentLookupKey() {
        return SundialHolder.getDataSourceType();
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