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

package com.github.yizzuide.milkomeda.light;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.universe.extend.web.handler.AstrolabeHandler;
import org.springframework.core.Ordered;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.Map;

/**
 * LightCacheCleanAstrolabeHandler
 * 超级缓存清理
 *
 * @author yizzuide
 * @since 3.3.0
 * @version 3.13.0
 * <br />
 * Create at 2020/05/06 14:13
 */
public class LightCacheCleanAstrolabeHandler implements AstrolabeHandler {

    private final LightThreadLocalScope scope;

    public LightCacheCleanAstrolabeHandler(LightThreadLocalScope scope) {
        this.scope = scope;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void postHandle(ServletRequest request, ServletResponse response) {
        // 清除请求线程的所有Cache子实例的超级缓存
        Map<String, Cache> cacheMap = ApplicationContextHolder.get().getBeansOfType(Cache.class, false, false);
        for (Cache cache : cacheMap.values()) {
            if (cache instanceof LightCache && ((LightCache)cache).isEnableSuperCache()) {
                CacheHelper.remove(cache);
            }
        }
        // 用户注册的LightContext bean
        Map<String, LightContext> lightContextMap = ApplicationContextHolder.get().getBeansOfType(LightContext.class);
        lightContextMap.forEach((k, v) -> v.remove());

        // 清空ThreadLocalScope
        if (this.scope != null) {
            this.scope.destroy();
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
