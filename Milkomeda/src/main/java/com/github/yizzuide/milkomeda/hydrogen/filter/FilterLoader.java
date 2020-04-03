package com.github.yizzuide.milkomeda.hydrogen.filter;

import com.github.yizzuide.milkomeda.hydrogen.core.HydrogenLoader;

import javax.servlet.Filter;
import javax.servlet.ServletContext;

/**
 * FilterLoader
 * 过滤器加载器
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/03 01:04
 */
public interface FilterLoader extends HydrogenLoader {

    /**
     * 设置Servlet上下文
     * @param servletContext    ServletContext
     */
    void setServletContext(ServletContext servletContext);

    /**
     * 动态加载一个Filter（仅适用于容器Tomcat)
     * @param name          过滤器名
     * @param clazz         过滤器类
     * @param urlPatterns   匹配路径
     * @return 加载是否成功
     */
    boolean load(String name, Class<? extends Filter> clazz, String... urlPatterns);

    /**
     * 动态删除一个Filter（仅适用于容器Tomcat)
     * @param name  过滤器名
     * @return 删除是否成功
     */
    boolean unload(String name);
}
