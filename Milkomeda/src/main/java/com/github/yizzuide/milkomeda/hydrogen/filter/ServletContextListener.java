package com.github.yizzuide.milkomeda.hydrogen.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletContextInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * ServletContextListener
 * ServletContext监听器
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/01 18:18
 */
public class ServletContextListener implements ServletContextInitializer {

    @Autowired
    private FilterLoader filterLoader;

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        filterLoader.setServletContext(servletContext);
    }
}
