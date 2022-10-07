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

package com.github.yizzuide.milkomeda.hydrogen.filter;

import com.github.yizzuide.milkomeda.universe.polyfill.TomcatPolyfill;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.ApplicationContextFacade;
import org.apache.catalina.core.ApplicationFilterFactory;
import org.apache.catalina.core.StandardContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TomcatFilterLoader
 * Tomcat过滤器加载器
 *
 * @author yizzuide
 * @since 3.0.0
 * @see ApplicationContextFacade#addFilter(java.lang.String, java.lang.String)
 * @see ApplicationContext#addFilter(java.lang.String, java.lang.Class)
 * @see StandardContext#filterStart()
 * @see ApplicationFilterFactory#createFilterChain(javax.servlet.ServletRequest, org.apache.catalina.Wrapper, javax.servlet.Servlet)
 * <br />
 * Create at 2020/04/01 18:19
 */
@Slf4j
public class TomcatFilterLoader extends AbstractFilterLoader<FilterProperties.Filters> {
    /**
     * 初始化预加载过滤器
     */
    private List<FilterProperties.Filters> filtersList;

    @Autowired
    private FilterProperties props;

    public boolean load(String name, Class<? extends Filter> clazz, String... urlPatterns) {
        // 初始时调用加载
        if (getApplicationContext() == null) {
            if (filtersList == null) {
                filtersList = new ArrayList<>();
            }
            if (urlPatterns == null) {
                urlPatterns =  new String[] {"/*"};
            }
            FilterProperties.Filters filters = new FilterProperties.Filters();
            filters.setName(name);
            filters.setClazz(clazz);
            filters.setUrlPatterns(Arrays.asList(urlPatterns));
            filtersList.add(filters);
            return true;
        }
        // 动态加载
        // 这个API无法unload
        //getServletContext().addFilter()
        return TomcatPolyfill.addDynamicFilter(getServletContext(), (ConfigurableApplicationContext) getApplicationContext(), name, clazz, urlPatterns);
    }

    public boolean unload(String name) {
        return TomcatPolyfill.removeDynamicFilter(name, getServletContext());
    }

    @Override
    public void refresh() {
        // 如果ServletContext还未加载完成，等待ServletContext加载完成事件
        if (this.getServletContext() == null) {
            return;
        }

        // 初始化Filter加载
        if (filtersList != null) {
            for (FilterProperties.Filters initFilters : filtersList) {
                load(initFilters.getName(), initFilters.getClazz(), initFilters.getUrlPatterns().toArray(new String[0]));
            }
            // clear...
            filtersList.clear();
            filtersList = null;
        }

        // 配置动态加载
        List<FilterProperties.Filters> filtersList = this.props.getFilters();
        merge(filtersList, f -> this.unload(f.getName()), f ->
                this.load(f.getName(), f.getClazz(), f.getUrlPatterns().toArray(new String[0])));
    }
}
