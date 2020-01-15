package com.github.yizzuide.milkomeda.light;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;

import javax.servlet.*;
import java.io.IOException;
import java.util.Map;

/**
 * LightCacheClearFilter
 *
 * @author yizzuide
 * @since  2.0.0
 * Create at 2019/12/19 11:19
 */
public class LightCacheClearFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        filterChain.doFilter(servletRequest, servletResponse);
        // 清除请求线程的所有Cache子实例的超级缓存
        Map<String, Cache> cacheMap = ApplicationContextHolder.get().getBeansOfType(Cache.class);
        for (Cache cache : cacheMap.values()) {
            CacheHelper.remove(cache);
        }
    }
}
