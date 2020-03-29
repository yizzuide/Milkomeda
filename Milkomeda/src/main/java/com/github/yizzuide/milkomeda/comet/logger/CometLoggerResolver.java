package com.github.yizzuide.milkomeda.comet.logger;

import javax.servlet.http.HttpServletRequest;

/**
 * CometLoggerResolver
 * 占位符解析器
 *
 * @author yizzuide
 * @since 2.8.0
 * Create at 2020/03/28 12:55
 */
@FunctionalInterface
public interface CometLoggerResolver {
    /**
     * 占位符解析
     * @param key       占位符
     * @param request   HttpServletRequest
     * @return  Object
     */
    Object resolver(String key, HttpServletRequest request);
}
