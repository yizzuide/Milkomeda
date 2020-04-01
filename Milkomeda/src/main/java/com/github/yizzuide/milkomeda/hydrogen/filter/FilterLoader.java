package com.github.yizzuide.milkomeda.hydrogen.filter;

import lombok.Data;
import org.apache.catalina.core.*;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import java.lang.reflect.Field;
import java.util.*;

/**
 * FilterLoader
 *
 * @author yizzuide
 * @since 3.0.0
 * @see ApplicationContextFacade#addFilter(java.lang.String, java.lang.String)
 * @see ApplicationContext#addFilter(java.lang.String, java.lang.Class)
 * @see StandardContext#filterStart()
 * @see ApplicationFilterFactory#createFilterChain(javax.servlet.ServletRequest, org.apache.catalina.Wrapper, javax.servlet.Servlet)
 * Create at 2020/04/01 18:19
 */
@Data
public class FilterLoader {

    private ServletContext servletContext;

    /**
     * 探查过滤器信息
     * @return 过滤器信息列表
     */
    public List<Map<String, String>> inspect() {
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, ? extends FilterRegistration> filterRegistrations = servletContext.getFilterRegistrations();
        for (Map.Entry<String, ? extends FilterRegistration> entry : filterRegistrations.entrySet()) {
            Map<String, String> filterInfoMap = new HashMap<>();
            filterInfoMap.put("name",  entry.getKey());
            FilterRegistration filterRegistration = entry.getValue();
            filterInfoMap.put("class", filterRegistration.getClassName());
            filterInfoMap.put("urlPattern", filterRegistration.getUrlPatternMappings().toString());
            list.add(filterInfoMap);
        }
        return list;
    }

    /**
     * 初始化加载一个Filter
     * @param name          过滤器名
     * @param filter        过滤器
     * @param urlPattern    匹配路径
     */
    public void load(String name, Class<? extends Filter> filter, String urlPattern) {
        FilterRegistration.Dynamic filterRegistration = servletContext.addFilter(name, filter);
        filterRegistration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, urlPattern);
    }

    /**
     * 动态加载一个Filter（仅用于Tomcat 9.0)
     * @param name          过滤器名
     * @param filter        过滤器
     * @param urlPattern    匹配路径
     * @return 加载是否成功
     */
    public boolean loadDynamic(String name, Class<? extends Filter> filter, String urlPattern) {
        load(name, filter, urlPattern);
        // boolean ((ApplicationContextFacade) servletContext).context.context.filterStart()
        return false;
    }

    /**
     * 动态删除一个Filter（仅用于Tomcat 9.0)
     * @param name  过滤器名
     */
    @SuppressWarnings("unchecked")
    public void unload(String name) {
        // ((ApplicationContextFacade) servletContext).context.context.filterConfigs (具体会读这个属性里的过滤器）
        try {
            Field contextFieldLevel1 = servletContext.getClass().getDeclaredField("context");
            contextFieldLevel1.setAccessible(true);
            Object contextLevel1 = contextFieldLevel1.get(servletContext);
            Field contextFieldLevel2 = contextLevel1.getClass().getDeclaredField("context");
            contextFieldLevel2.setAccessible(true);
            Object contextLevel2 = contextFieldLevel2.get(contextLevel1);

            Field filterConfigsFiled = contextLevel2.getClass().getDeclaredField("filterConfigs");
            filterConfigsFiled.setAccessible(true);
            Map<String, ApplicationFilterConfig> filterConfigs = (Map<String, ApplicationFilterConfig>) filterConfigsFiled.get(contextLevel2);
            filterConfigs.remove(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
