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

package com.github.yizzuide.milkomeda.universe.polyfill;

import com.github.yizzuide.milkomeda.universe.context.SpringContext;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.catalina.core.ApplicationFilterConfig;
import org.apache.catalina.core.ApplicationFilterRegistration;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.springframework.context.ConfigurableApplicationContext;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import java.util.EnumSet;
import java.util.Map;

/**
 * TomcatPolyfill
 *
 * @author yizzuide
 * @since 3.0.0
 * <br>
 * Create at 2020/04/03 11:32
 */
@Slf4j
public class TomcatPolyfill {

    /***
     * 动态添加过滤器
     * @param servletContext     ServletContext
     * @param applicationContext 应用上下文
     * @param name               过滤器名
     * @param clazz              过滤器类
     * @param urlPattern         过滤路径
     * @return 添加是否成功
     */
    public static boolean addDynamicFilter(ServletContext servletContext, ConfigurableApplicationContext applicationContext,  String name, Class<? extends Filter> clazz, String... urlPattern) {
        Filter filterBean = SpringContext.registerBean(applicationContext, name, clazz);
        applicationContext.getAutowireCapableBeanFactory().autowireBean(filterBean);
        Context standContext = ReflectUtil.invokeFieldPath(servletContext, "context.context");
        if (standContext == null) {
            log.warn("Hydrogen filter find path '((ApplicationContextFacade) servletContext).context.context' fail.");
            return false;
        }
        // ((ApplicationContextFacade) servletContext).context.context.filterDefs（filterMaps和filterConfigs的数据源，并提供给外部接口访问）
        FilterDef filterDef = ReflectUtil.invokeMethod(standContext, "findFilterDef", new Class[]{String.class}, name);
        if (filterDef != null) {
            return false;
        }
        filterDef = new FilterDef();
        filterDef.setFilterName(name);
        ReflectUtil.invokeMethod(standContext, "addFilterDef", new Class[]{FilterDef.class}, filterDef);
        filterDef.setFilterClass(filterBean.getClass().getName());
        filterDef.setFilter(filterBean);
        // 在内部创建filterMaps（作为请求调用链的路径映射过滤器集合）
        FilterRegistration.Dynamic filterRegistration = new ApplicationFilterRegistration(filterDef, standContext);
        filterRegistration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, urlPattern);

        // boolean ((ApplicationContextFacade) servletContext).context.context.filterStart() (把filterDefs加载到filterConfigs)
        return (boolean) ReflectUtil.invokeMethod(standContext, "filterStart", null);
    }

    /**
     * 动态删除过滤器
     * @param name              过滤器名
     * @param servletContext    ServletContext
     * @return  删除是否成功
     */
    public static boolean removeDynamicFilter(String name, ServletContext servletContext) {
        Context standContext = ReflectUtil.invokeFieldPath(servletContext, "context.context");
        if (standContext == null) {
            log.warn("Hydrogen filter find path '((ApplicationContextFacade) servletContext).context.context' fail.");
            return false;
        }

        // ((ApplicationContextFacade) servletContext).context.context.filterMaps（在filterChain里根据映射的filterMaps转filterConfigs）
        FilterMap[] filterMaps = ReflectUtil.invokeMethod(standContext, "findFilterMaps", null);
        if (filterMaps == null) {
            log.warn("Hydrogen filter find path '((ApplicationContextFacade) servletContext).context.context.findFilterMaps()' fail.");
            return false;
        }
        for (FilterMap filterMap : filterMaps) {
            if (filterMap.getFilterName().equals(name)) {
                ReflectUtil.invokeMethod(standContext, "removeFilterMap", new Class[]{FilterMap.class}, filterMap);
                break;
            }
        }

        // ((ApplicationContextFacade) servletContext).context.context.filterDefs（filterMaps和filterConfigs的数据源，并提供给外部接口访问）
        FilterDef filterDef = ReflectUtil.invokeMethod(standContext, "findFilterDef", new Class[]{String.class}, name);
        if (filterDef != null) {
            ReflectUtil.invokeMethod(standContext, "removeFilterDef", new Class[]{FilterDef.class}, filterDef);
        }

        // ((ApplicationContextFacade) servletContext).context.context.filterConfigs (FilterChain具体会读这个属性里的过滤器）
        Map<String, ApplicationFilterConfig> filterConfigs = ReflectUtil.invokeFieldPath(standContext, "filterConfigs");
        if (filterConfigs == null) {
            log.warn("Hydrogen filter find path '((ApplicationContextFacade) servletContext).context.context.filterConfigs' fail.");
            return false;
        }
        filterConfigs.remove(name);

        // boolean ((ApplicationContextFacade) servletContext).context.context.filterStart() (把filterDefs加载到filterConfigs)
        return (boolean) ReflectUtil.invokeMethod(standContext, "filterStart", null);
    }
}
