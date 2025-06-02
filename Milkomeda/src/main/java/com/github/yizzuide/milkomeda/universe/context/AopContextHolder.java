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

package com.github.yizzuide.milkomeda.universe.context;

import com.github.yizzuide.milkomeda.comet.core.CometAspect;
import com.github.yizzuide.milkomeda.comet.core.CometInterceptor;
import com.github.yizzuide.milkomeda.comet.core.WebCometData;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AopContext;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/**
 * AopContextHolder
 *
 * @author yizzuide
 * @since 1.13.4
 * @version 3.21.0
 * <br>
 * Create at 2019/10/24 21:17
 */
public final class AopContextHolder {
    /**
     * 获得当前切面代理对象
     *
     * @param clazz 当前类
     * @param <T>   当前类型
     * @return  代理对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T self(Class<T> clazz) {
        return  (T)AopContext.currentProxy();
    }

    /**
     * 获取被代理的Bean目标对象
     * @param proxy Bean代理对象
     * @param clazz Bean class
     * @return  Bean目标对象
     * @param <T> Bean类型
     * @since 3.21.0
     */
    @SuppressWarnings("unchecked")
    public static <T> T getRealTarget(Object proxy, Class<T> clazz) {
        Object target = null;
        if (AopUtils.isAopProxy(proxy)) {
            target = AopProxyUtils.getSingletonTarget(proxy);
        }
        if (target == null && proxy instanceof Advised) {
            try {
                target = ((Advised)proxy).getTargetSource().getTarget();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return (T)target;
    }

    /**
     * 获取被代理的Bean目标对象
     * @param clazz Bean class
     * @return  Bean目标对象
     * @param <T>   Bean类型
     * @since 3.21.0
     */
    public static <T> T getRealTarget(Class<T> clazz) {
        T proxy = ApplicationContextHolder.get().getBean(clazz);
        return getRealTarget(proxy, clazz);
    }

    /**
     * 设置当前事务回滚
     * @since 3.21.0
     */
    public static void transactionRollback() {
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }

    /**
     * 获取控制层采集数据
     *
     * @return WebCometData
     */
    public static WebCometData getWebCometData() {
        return getWebCometData(true);
    }

    /**
     * 获取控制层采集数据
     * @param useTag    是否采用的TagCollector方式
     * @return  WebCometData
     * @since 3.12.10
     */
    public static WebCometData getWebCometData(boolean useTag) {
        // 方法注解采集（注解方式）
        if (!useTag) {
            return CometAspect.getCurrentWebCometData();
        }
        // 下面保留之前的逻辑
        // 拦截器层采集（用于TagCollector)
        WebCometData webCometData = CometInterceptor.getWebCometData();
        if (webCometData == null) {
            // 方法注解采集（注解方式）
            return CometAspect.getCurrentWebCometData();
        }
        return webCometData;
    }
}
