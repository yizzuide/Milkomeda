package com.github.yizzuide.milkomeda.hydrogen.filter;

import com.github.yizzuide.milkomeda.universe.polyfill.TomcatPolyfill;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.ApplicationContextFacade;
import org.apache.catalina.core.ApplicationFilterFactory;
import org.apache.catalina.core.StandardContext;

import javax.servlet.Filter;

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
 * Create at 2020/04/01 18:19
 */
@Slf4j
public class TomcatFilterLoader extends AbstractFilterLoader {

    public boolean load(String name, Class<? extends Filter> clazz, String urlPattern) {
        return TomcatPolyfill.addDynamicFilter(name, clazz, urlPattern, getServletContext());
    }

    public boolean unload(String name) {
        return TomcatPolyfill.removeDynamicFilter(name, getServletContext());
    }

    @Override
    protected void refresh() {

    }
}
