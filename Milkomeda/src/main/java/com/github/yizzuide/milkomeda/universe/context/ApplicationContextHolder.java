package com.github.yizzuide.milkomeda.universe.context;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * ApplicationContextHolder
 *
 * @author yizzuide
 * @since 0.2.1
 * @version 1.5.0
 * Create at 2019/04/12 11:04
 */
public class ApplicationContextHolder implements ApplicationContextAware {
    @Getter @Setter
    private ApplicationContext applicationContext;

    private static ApplicationContextHolder INSTANCE;

    public ApplicationContextHolder() {
        INSTANCE = this;
    }

    /**
     * 获取Spring IOC上下文
     * @return ApplicationContext
     */
    public static ApplicationContext get() {
        return INSTANCE.getApplicationContext();
    }
}
