package com.github.yizzuide.milkomeda.universe.context;

import com.github.yizzuide.milkomeda.universe.el.ELContext;
import com.github.yizzuide.milkomeda.universe.env.Environment;
import lombok.Getter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;

/**
 * ApplicationContextHolder
 *
 * @author yizzuide
 * @since 0.2.1
 * @version 3.1.0
 * Create at 2019/04/12 11:04
 */
public class ApplicationContextHolder implements ApplicationContextAware {

    private static ApplicationContextHolder INSTANCE;

    private static Environment environment;

    @Getter
    private ApplicationContext applicationContext;


    public ApplicationContextHolder() {
        INSTANCE = this;
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        ELContext.setApplicationContext(applicationContext);
        if (applicationContext instanceof ConfigurableApplicationContext) {
            ApplicationContextHolder.environment.setConfigurableEnvironment(((ConfigurableApplicationContext) applicationContext).getEnvironment());
        }
    }

    /**
     * 获取Spring IOC上下文
     * @return ApplicationContext
     */
    public static ApplicationContext get() {
        return INSTANCE.getApplicationContext();
    }

    public static void setEnvironment(Environment environment) {
        ApplicationContextHolder.environment = environment;
    }

    /**
     * 获取Spring环境变量
     * @return  Environment
     */
    public static Environment getEnvironment() {
        return environment;
    }
}
