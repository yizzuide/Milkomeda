package com.github.yizzuide.milkomeda.universe.context;

import com.github.yizzuide.milkomeda.universe.el.ELContext;
import lombok.Getter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * ApplicationContextHolder
 *
 * @author yizzuide
 * @since 0.2.1
 * @version 2.0.0
 * Create at 2019/04/12 11:04
 */
public class ApplicationContextHolder implements ApplicationContextAware {
    @Getter
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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        ELContext.setApplicationContext(applicationContext);
    }
}
