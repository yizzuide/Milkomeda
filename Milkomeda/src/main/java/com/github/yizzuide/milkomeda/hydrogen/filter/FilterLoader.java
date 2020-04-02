package com.github.yizzuide.milkomeda.hydrogen.filter;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import java.util.List;
import java.util.Map;

/**
 * FilterLoader
 * 过滤器加载器
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/03 01:04
 */
public interface FilterLoader {

    /***
     * 设置Servlet上下文
     * @param servletContext    ServletContext
     */
    void setServletContext(ServletContext servletContext);

    /**
     * 探查过滤器信息
     * @return 过滤器信息列表
     */
    List<Map<String, String>> inspect();

    /**
     * 动态加载一个Filter（仅适用于容器Tomcat)
     * @param name          过滤器名
     * @param clazz         过滤器类
     * @param urlPattern    匹配路径
     * @return 加载是否成功
     */
    boolean load(String name, Class<? extends Filter> clazz, String urlPattern);

    /**
     * 动态删除一个Filter（仅适用于容器Tomcat)
     * @param name  过滤器名
     * @return 删除是否成功
     */
    boolean unload(String name);
}
