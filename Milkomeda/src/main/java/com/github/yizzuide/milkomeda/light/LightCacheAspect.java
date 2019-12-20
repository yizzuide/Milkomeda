package com.github.yizzuide.milkomeda.light;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.annotation.Annotation;

import static com.github.yizzuide.milkomeda.util.ReflectUtil.extractValue;
import static com.github.yizzuide.milkomeda.util.ReflectUtil.getAnnotation;

/**
 * LightCacheAspect
 *
 * @author yizzuide
 * @since 2.0.0
 * Create at 2019/12/18 14:45
 */
@Order(98)
@Aspect
public class LightCacheAspect {

    public static final String DEFAULT_BEAN_NAME = "lightCache";

    @Pointcut("@annotation(com.github.yizzuide.milkomeda.light.LightCacheable)")
    public void cacheablePointCut() {}

    @Pointcut("@annotation(com.github.yizzuide.milkomeda.light.LightCacheEvict)")
    public void cacheEvictPointCut() {}

    @Pointcut("@annotation(com.github.yizzuide.milkomeda.light.LightCachePut)")
    public void cachePutPointCut() {}

    @Around("cacheablePointCut()")
    public Object cacheableAround(ProceedingJoinPoint joinPoint) throws Throwable {
        val cacheable = getAnnotation(joinPoint, LightCacheable.class);
        // 检查缓存条件
        String condition = cacheable.condition();
        if (!StringUtils.isEmpty(condition) && !Boolean.parseBoolean(extractValue(joinPoint, condition))) {
            return joinPoint.proceed();
        }
        return applyAround(joinPoint, cacheable, cacheable.value(), cacheable.keyPrefix(), cacheable.key(), cacheable.gKey());
    }

    @Around("cacheEvictPointCut()")
    public Object cacheEvictAround(ProceedingJoinPoint joinPoint) throws Throwable {
        val cacheEvict = getAnnotation(joinPoint, LightCacheEvict.class);
        return applyAround(joinPoint, cacheEvict, cacheEvict.value(), cacheEvict.keyPrefix(), cacheEvict.key(), cacheEvict.gKey());
    }

    @Around("cachePutPointCut()")
    public Object cachePutAround(ProceedingJoinPoint joinPoint) throws Throwable {
        val cachePut = getAnnotation(joinPoint, LightCachePut.class);
        return applyAround(joinPoint, cachePut, cachePut.value(), cachePut.keyPrefix(), cachePut.key(), cachePut.gKey());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object applyAround(ProceedingJoinPoint joinPoint, Annotation annotation, String cacheBeanName, String prefix, String key, String gKey) throws Throwable {
        if (StringUtils.isEmpty(key) && StringUtils.isEmpty(gKey)) {
            throw new IllegalArgumentException(String.format("You must set key before use %s.", annotation.annotationType().getSimpleName()));
        }
        boolean isUsedGKey = StringUtils.isEmpty(key);
        // 解析表达式
        key = extractValue(joinPoint, key);
        Serializable view = isUsedGKey ? gKey : key;
        Cache defaultBean = ApplicationContextHolder.get().getBean(DEFAULT_BEAN_NAME, LightCache.class);
        Cache cache = defaultBean;
        // 如果有自定义的bean名，创建动态的Cache对象
        if (!DEFAULT_BEAN_NAME.equals(cacheBeanName)) {
            cacheBeanName = cacheBeanName + (int) (Math.random() * 9 + 1) * (2 << 10);
            cache = WebContext.registerBean((ConfigurableApplicationContext) ApplicationContextHolder.get(), cacheBeanName, LightCache.class);
            BeanUtils.copyProperties(defaultBean, cache);
        }
        // 根据条件移除缓存数据
        if (annotation.annotationType() == LightCachePut.class ||
                annotation.annotationType() == LightCacheEvict.class) {
            CacheHelper.erase(cache, view, isUsedGKey ? id -> gKey : id -> prefix + id);
            // 删除类型直接返回
            if (annotation.annotationType() == LightCacheEvict.class) {
                return null;
            }
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class eType = signature.getReturnType();
        return CacheHelper.get(cache, eType, view, isUsedGKey ? id -> gKey : id -> prefix + id, id -> joinPoint.proceed());
    }
}
