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
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.universe.el.ELContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.function.Function;

import static com.github.yizzuide.milkomeda.util.ReflectUtil.extractValue;

/**
 * LightCacheAspect
 * <br>
 * Aspect注解会被AbstractAutoProxyCreator创建Proxy对象，而@Async会被AsyncAnnotationBeanPostProcessor创建。
 * 它们都继承了ProxyProcessorSupport，AsyncAnnotationBeanPostProcessor在有Proxy对象时只添加Advice。
 *
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator
 * @see org.springframework.scheduling.annotation.AsyncAnnotationBeanPostProcessor
 * @see org.springframework.aop.framework.AbstractAdvisingBeanPostProcessor
 *
 * @author yizzuide
 * @since 2.0.0
 * @version 3.12.4
 * Create at 2019/12/18 14:45
 */
@Order(98)
@Aspect
public class LightCacheAspect {
    public static final String DEFAULT_BEAN_NAME = "lightCache";

    @Autowired
    private LightProperties props;

    @Around("execution(@LightCacheable * *.*(..)) && @annotation(cacheable)")
    public Object cacheableAround(ProceedingJoinPoint joinPoint, LightCacheable cacheable) throws Throwable {
        return applyAround(joinPoint, cacheable, cacheable.condition(), cacheable.value(), cacheable.keyPrefix(), cacheable.key());
    }

    @Around("execution(@LightCacheEvict * *.*(..)) && @annotation(cacheEvict)")
    public Object cacheEvictAround(ProceedingJoinPoint joinPoint, LightCacheEvict cacheEvict) throws Throwable {
        return applyAround(joinPoint, cacheEvict, cacheEvict.condition(), cacheEvict.value(), cacheEvict.keyPrefix(), cacheEvict.key());
    }

    @Around("execution(@LightCachePut * *.*(..)) && @annotation(cachePut)")
    public Object cachePutAround(ProceedingJoinPoint joinPoint, LightCachePut cachePut) throws Throwable {
        return applyAround(joinPoint, cachePut, cachePut.condition(), cachePut.value(), cachePut.keyPrefix(), cachePut.key());
    }

    @SuppressWarnings("unchecked")
    private Object applyAround(ProceedingJoinPoint joinPoint, Annotation annotation, String condition, String cacheBeanName, String prefix, String key) throws Throwable {
        // 检查缓存条件
        if (!StringUtils.isEmpty(condition) && !Boolean.parseBoolean(ELContext.getValue(joinPoint, condition))) {
            return joinPoint.proceed();
        }

        if (StringUtils.isEmpty(key)) {
            throw new IllegalArgumentException(String.format("You must set key before use %s.", annotation.annotationType().getSimpleName()));
        }

        // 解析缓存实例名
        cacheBeanName = extractValue(joinPoint, cacheBeanName);

        // 解析表达式
        String viewId = extractValue(joinPoint, key);
        LightCache cache;
        String originCacheBeanName = cacheBeanName;
        // 修改Bean name，防止与开发者项目里重复
        cacheBeanName = innerCacheBeanName(cacheBeanName);
        if (ApplicationContextHolder.get().containsBean(cacheBeanName)) {
            cache = ApplicationContextHolder.get().getBean(cacheBeanName, LightCache.class);
        } else {
            cache = WebContext.registerBean((ConfigurableApplicationContext) ApplicationContextHolder.get(), cacheBeanName, LightCache.class);
            // 自定义缓存实例配置
            if (props.getInstances().containsKey(originCacheBeanName)) {
                cache.configFrom(props.getInstances().get(originCacheBeanName));
            } else {
                // 否则拷贝默认的配置
                LightCache defaultBean = ApplicationContextHolder.get().getBean(DEFAULT_BEAN_NAME, LightCache.class);
                cache.copyFrom(defaultBean);
            }
        }

        // key生成器
        Function<Serializable, String> keyGenerator = id -> prefix + id;

        // 删除类型
        if (annotation.annotationType() == LightCacheEvict.class) {
            // 缓存读写策略 - Cache Aside (先删除数据源，再删除缓存）
            joinPoint.proceed();
            CacheHelper.erase(cache, viewId, keyGenerator);
            return null;
        }

        // 更新类型也是先更新数据源，再更新缓存
        if (annotation.annotationType() == LightCachePut.class) {
            return CacheHelper.put(cache, viewId, keyGenerator, id -> joinPoint.proceed());
        }
        // 获取类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return CacheHelper.get(cache, signature.getReturnType(), viewId, keyGenerator, id -> joinPoint.proceed());
    }

    /**
     * 转为内部缓存Bean名
     * @param cacheBeanName 原bean名
     * @return  内部缓存Bean名
     */
    String innerCacheBeanName(String cacheBeanName) {
        return DEFAULT_BEAN_NAME + "_" + cacheBeanName;
    }
}
