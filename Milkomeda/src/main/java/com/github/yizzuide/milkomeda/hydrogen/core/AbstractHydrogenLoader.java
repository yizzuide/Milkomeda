package com.github.yizzuide.milkomeda.hydrogen.core;

import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.EventListener;
import org.springframework.util.CollectionUtils;

/**
 * AbstractHydrogenLoader
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/03 11:16
 */
@Data
public abstract class AbstractHydrogenLoader implements HydrogenLoader, ApplicationContextAware {
    /**
     * 应用上下文
     */
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        // 刷新拦截器
        refresh();
    }

    @EventListener
    public void configListener(EnvironmentChangeEvent event) {
        // 键没有修改，直接返回
        if (CollectionUtils.isEmpty(event.getKeys())) {
            return;
        }
        refresh();
    }

    /**
     * 刷新配置后加载拦截器
     */
    protected abstract void refresh();
}
