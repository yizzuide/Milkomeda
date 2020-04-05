package com.github.yizzuide.milkomeda.hydrogen.interceptor;

import org.springframework.util.PathMatcher;
import org.springframework.web.util.UrlPathHelper;

/**
 * InterceptorHolder
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/06 01:14
 */
class InterceptorHolder {

    /**
     * 路径匹配器
     */
    private static PathMatcher mvcPathMatcher;

    /**
     * URL路径帮助类
     */
    private static UrlPathHelper urlPathHelper;

    static void setMvcPathMatcher(PathMatcher mvcPathMatcher) {
        InterceptorHolder.mvcPathMatcher = mvcPathMatcher;
    }

    static PathMatcher getMvcPathMatcher() {
        return mvcPathMatcher;
    }

    static void setUrlPathHelper(UrlPathHelper urlPathHelper) {
        InterceptorHolder.urlPathHelper = urlPathHelper;
    }

    static UrlPathHelper getUrlPathHelper() {
        return urlPathHelper;
    }
}
