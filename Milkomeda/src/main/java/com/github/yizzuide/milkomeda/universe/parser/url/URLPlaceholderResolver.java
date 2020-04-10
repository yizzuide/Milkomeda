package com.github.yizzuide.milkomeda.universe.parser.url;

import javax.servlet.http.HttpServletRequest;

/**
 * URLPlaceholderResolver
 * URL占位符解析器
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/03/28 12:55
 */
@FunctionalInterface
public interface URLPlaceholderResolver {
    /**
     * 占位符解析
     * @param key       占位符
     * @param request   HttpServletRequest
     * @return  Object
     */
    Object resolver(String key, HttpServletRequest request);
}
