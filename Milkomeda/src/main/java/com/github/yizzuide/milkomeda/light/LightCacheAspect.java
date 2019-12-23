package com.github.yizzuide.milkomeda.light;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.function.Function;

import static com.github.yizzuide.milkomeda.util.ReflectUtil.extractValue;

/**
 * LightCacheAspect
 *
 * @author yizzuide
 * @since 2.0.0
 * @version 2.0.1
 * Create at 2019/12/18 14:45
 */
@Order(98)
@Aspect
public class LightCacheAspect {
    public static final String DEFAULT_BEAN_NAME = "lightCache";

    @Around("execution(@LightCacheable * *.*(..)) && @annotation(cacheable)")
    public Object cacheableAround(ProceedingJoinPoint joinPoint, LightCacheable cacheable) throws Throwable {
        return applyAround(joinPoint, cacheable, cacheable.condition(), cacheable.value(), cacheable.keyPrefix(), cacheable.key(), cacheable.gKey());
    }

    @Around("execution(@LightCacheEvict * *.*(..)) && @annotation(cacheEvict)")
    public Object cacheEvictAround(ProceedingJoinPoint joinPoint, LightCacheEvict cacheEvict) throws Throwable {
        return applyAround(joinPoint, cacheEvict, cacheEvict.condition(), cacheEvict.value(), cacheEvict.keyPrefix(), cacheEvict.key(), cacheEvict.gKey());
    }

    @Around("execution(@LightCachePut * *.*(..)) && @annotation(cachePut)")
    public Object cachePutAround(ProceedingJoinPoint joinPoint, LightCachePut cachePut) throws Throwable {
        return applyAround(joinPoint, cachePut, cachePut.condition(), cachePut.value(), cachePut.keyPrefix(), cachePut.key(), cachePut.gKey());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object applyAround(ProceedingJoinPoint joinPoint, Annotation annotation, String condition, String cacheBeanName, String prefix, String key, String gKey) throws Throwable {
        // 检查缓存条件
        if (!StringUtils.isEmpty(condition) && !Boolean.parseBoolean(extractValue(joinPoint, condition))) {
            return joinPoint.proceed();
        }
        if (StringUtils.isEmpty(key) && StringUtils.isEmpty(gKey)) {
            throw new IllegalArgumentException(String.format("You must set key before use %s.", annotation.annotationType().getSimpleName()));
        }
        boolean isUsedGKey = StringUtils.isEmpty(key);
        // 解析表达式
        key = extractValue(joinPoint, key);
        Serializable view = isUsedGKey ? gKey : key;
        LightCache defaultBean = ApplicationContextHolder.get().getBean(DEFAULT_BEAN_NAME, LightCache.class);
        LightCache cache = defaultBean;
        // 如果有自定义的bean名，创建动态的Cache对象
        if (!DEFAULT_BEAN_NAME.equals(cacheBeanName)) {
            cache = WebContext.registerBean((ConfigurableApplicationContext) ApplicationContextHolder.get(), cacheBeanName, LightCache.class);
            // 判断是否拷贝默认的配置
            if (annotation.annotationType() == LightCacheable.class) {
                LightCacheable cacheable = (LightCacheable) annotation;
                if (cacheable.copyDefaultConfig()) {
                    BeanUtils.copyProperties(defaultBean, cache);
                }
            }
        }
        Function<Serializable, String> keyGenerator = isUsedGKey ? id -> gKey : id -> prefix + id;
        // 删除类型直接返回
        if (annotation.annotationType() == LightCacheEvict.class) {
            // 缓存读写策略 - Cache Aside (先删除数据库，再删除缓存）
            joinPoint.proceed();
            CacheHelper.erase(cache, view, keyGenerator);
            return null;
        }
        // 更新类型也是先更新数据库，再更新缓存
        if (annotation.annotationType() == LightCachePut.class) {
            return CacheHelper.put(cache, view, keyGenerator, id -> joinPoint.proceed());
        }

        LightCacheable cacheable = (LightCacheable) annotation;
        if (cacheable.discardStrategy() != LightDiscardStrategy.DEFAULT) {
            cache.setStrategy(cacheable.discardStrategy());
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class eType = signature.getReturnType();
        return CacheHelper.get(cache, eType, view, keyGenerator, id -> joinPoint.proceed());
    }
}
