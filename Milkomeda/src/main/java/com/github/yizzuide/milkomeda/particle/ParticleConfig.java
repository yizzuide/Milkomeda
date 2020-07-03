package com.github.yizzuide.milkomeda.particle;

import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.util.IOUtils;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.web.filter.DelegatingFilterProxy;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ParticleConfig
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 3.10.0
 * Create at 2019/11/11 11:26
 */
@Slf4j
@Configuration
@ConditionalOnClass(RedisTemplate.class)
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties(ParticleProperties.class)
public class ParticleConfig implements ApplicationContextAware {

    @Autowired
    private ParticleProperties particleProperties;

    /**
     * 缓存创建的LimitHandler
     */
    private static final Map<String, LimitHandler> cacheHandlerBeans = new HashMap<>();

    @Bean
    public ParticleAspect particleAspect() {
        return new ParticleAspect();
    }

    @Bean
    public IdempotentLimiter idempotentLimiter() {
        return new IdempotentLimiter();
    }

    @Bean
    public ParticleFilter particleFilter() {
        return new ParticleFilter();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Bean
    @ConditionalOnProperty(prefix = "milkomeda.particle", name = "enable-filter", havingValue = "true")
    public FilterRegistrationBean particleFilterRegistrationBean() {
        FilterRegistrationBean particleFilterRegistrationBean = new FilterRegistrationBean();
        // 设置代理注册的Bean
        particleFilterRegistrationBean.setFilter(new DelegatingFilterProxy("particleFilter"));
        particleFilterRegistrationBean.setName("particleFilter");
        particleFilterRegistrationBean.setUrlPatterns(Collections.singleton("/*"));
        particleFilterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        return particleFilterRegistrationBean;
    }

    @SneakyThrows
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        List<ParticleProperties.Limiter> limiters = particleProperties.getLimiters();
        if (CollectionUtils.isEmpty(limiters)) {
            return;
        }
        List<ParticleProperties.Limiter> barrierLimiters = new ArrayList<>();
        Map<String, LimitHandler> shareLimitHandlerMap = new HashMap<>();
        for (ParticleProperties.Limiter limiter : limiters) {
            String limiterName = limiter.getName();
            // 使用枚举类型填充处理器clazz
            if (limiter.getHandlerClazz() == null && limiter.getType() != null) {
                switch (limiter.getType()) {
                    case IDEMPOTENT:
                        limiter.setHandlerClazz(IdempotentLimiter.class);
                        break;
                    case TIMES:
                        limiter.setHandlerClazz(TimesLimiter.class);
                        break;
                    case BARRIER:
                        limiter.setHandlerClazz(BarrierLimiter.class);
                        break;
                    case BLOOM:
                        limiter.setHandlerClazz(BloomLimiter.class);
                        break;
                }
            }
            LimitHandler limitHandler;
            String handlerKey = limiter.getHandlerClazz().getSimpleName();
            // 共享同类型模式
            if (limiter.isShareMode() && shareLimitHandlerMap.containsKey(handlerKey)) {
                limitHandler = shareLimitHandlerMap.get(handlerKey);
            } else {
                limitHandler = WebContext.registerBean((ConfigurableApplicationContext) applicationContext, limiterName, limiter.getHandlerClazz());
                shareLimitHandlerMap.put(handlerKey, limitHandler);
            }
            cacheHandlerBeans.put(limiterName, limitHandler);
            if (!CollectionUtils.isEmpty(limiter.getProps())) {
                ReflectUtil.setField(limitHandler, limiter.getProps());
            }
            limiter.setLimitHandler(limitHandler);
            if (limiter.getHandlerClazz() == BarrierLimiter.class) {
                barrierLimiters.add(limiter);
            }
        }
        shareLimitHandlerMap.clear();

        // 创建barrierLimiter类型限制器链
        for (ParticleProperties.Limiter limiter : barrierLimiters) {
            BarrierLimiter barrierLimiter = (BarrierLimiter) limiter.getLimitHandler();
            List<String> chain = barrierLimiter.getChain();
            if (CollectionUtils.isEmpty(chain)) {
                log.warn("Particle add barrier limiter find chain is empty, make true add at least one limiter.");
                continue;
            }
            List<LimitHandler> handlers = new ArrayList<>();
            for (String name : chain) {
                handlers.add(cacheHandlerBeans.get(name));
            }
            barrierLimiter.addLimitHandlerList(handlers);
        }

        // 根据order排序
        List<ParticleProperties.Limiter> orderLimiters = limiters.stream()
                .sorted(OrderComparator.INSTANCE.withSourceProvider(limiter -> limiter)).collect(Collectors.toList());
        particleProperties.setLimiters(orderLimiters);

        // 读取lua脚本
        String luaScript = IOUtils.loadLua("/META-INF/scripts", "particle_times_limiter.lua");
        TimesLimiter.setLuaScript(luaScript);
    }

    static Map<String, LimitHandler> getCacheHandlerBeans() {
        return cacheHandlerBeans;
    }
}
