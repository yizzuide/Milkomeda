package com.github.yizzuide.milkomeda.light;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
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
 * @version 2.0.3
 * Create at 2019/12/18 14:45
 */
@Order(98)
@Aspect
public class LightCacheAspect {
    public static final String DEFAULT_BEAN_NAME = "lightCache";

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
        if (!StringUtils.isEmpty(condition) && !Boolean.parseBoolean(extractValue(joinPoint, condition))) {
            return joinPoint.proceed();
        }
        if (StringUtils.isEmpty(key)) {
            throw new IllegalArgumentException(String.format("You must set key before use %s.", annotation.annotationType().getSimpleName()));
        }

        // 解析表达式
        String viewId = extractValue(joinPoint, key);
        LightCache defaultBean = ApplicationContextHolder.get().getBean(DEFAULT_BEAN_NAME, LightCache.class);
        LightCache cache = defaultBean;
        // 如果有自定义的bean名，创建动态的Cache对象
        if (!DEFAULT_BEAN_NAME.equals(cacheBeanName)) {
            // 修改Bean name，防止与开发者项目里重复
            cacheBeanName += DEFAULT_BEAN_NAME + "_" + cacheBeanName;
            cache = WebContext.registerBean((ConfigurableApplicationContext) ApplicationContextHolder.get(), cacheBeanName, LightCache.class);
            // 针对LightCacheable类型的处理
            if (annotation.annotationType() == LightCacheable.class && cache.getL1MaxCount() == null) {
                LightCacheable cacheable = (LightCacheable) annotation;
                if (cacheable.copyDefaultConfig()) {
                    // 拷贝默认的配置
                    cache.copyFrom(defaultBean);
                }
            }
        }
        Function<Serializable, String> keyGenerator = id -> prefix + id;
        // 删除类型直接返回
        if (annotation.annotationType() == LightCacheEvict.class) {
            // 缓存读写策略 - Cache Aside (先删除数据库，再删除缓存）
            joinPoint.proceed();
            CacheHelper.erase(cache, viewId, keyGenerator);
            return null;
        }

        // 设置自定义配置
        LightCacheable cacheable = (LightCacheable) annotation;
        if (cacheable.discardStrategy() != LightDiscardStrategy.DEFAULT) {
            cache.setStrategy(cacheable.discardStrategy());
            // 如果当前有设定过期时间（默认走配置文件）
            if (cacheable.expire() != -1) {
                // 如果 discardStrategy 为 LazyExpire 策略，自动同步配置到l2Expire过期
                if (cacheable.discardStrategy() == LightDiscardStrategy.LazyExpire) {
                    cache.setL1Expire(cacheable.expire() / 1000);
                } else {
                    // 其它排行类型，设置到二级缓存（一级缓存使用排行丢弃方案）
                    cache.setL2Expire(cacheable.expire() / 1000);
                }
            }
        }

        // 更新类型也是先更新数据库，再更新缓存
        if (annotation.annotationType() == LightCachePut.class) {
            return CacheHelper.put(cache, viewId, keyGenerator, id -> joinPoint.proceed());
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return CacheHelper.get(cache, signature.getReturnType(), viewId, keyGenerator, id -> joinPoint.proceed());
    }
}
