package com.github.yizzuide.milkomeda.light;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.universe.handler.AstrolabeHandler;
import org.springframework.core.Ordered;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.Map;

/**
 * CrustSessionCleanAstrolabeHandler
 * 超级缓存清理
 *
 * @author yizzuide
 * @since 3.12.4
 * Create at 2020/05/06 14:13
 */
public class LightCacheCleanAstrolabeHandler implements AstrolabeHandler {

    @Override
    public void postHandle(ServletRequest request, ServletResponse response) {
        // 清除请求线程的所有Cache子实例的超级缓存
        Map<String, LightCache> cacheMap = ApplicationContextHolder.get().getBeansOfType(LightCache.class);
        for (LightCache cache : cacheMap.values()) {
            if (cache.isEnableSuperCache()) {
                CacheHelper.remove(cache);
            }
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
