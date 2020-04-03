package com.github.yizzuide.milkomeda.hydrogen.core;

import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.EventListener;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * AbstractHydrogenLoader
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/03 11:16
 */
@Data
public abstract class AbstractHydrogenLoader<T> implements HydrogenLoader, ApplicationContextAware {
    /**
     * 应用上下文
     */
    private ApplicationContext applicationContext;

    /**
     * 加载过的配置处理器
     */
    private List<T> loadConfigHandlerList;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;

        // 刷新处理器列表
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
     * 合并当前配置处理器列表
     * @param configHandlerList 配置处理器列表
     * @param unload            卸载处理器函数
     * @param load              装载处理器函数
     */
    protected void merge(List<T> configHandlerList, Consumer<T> unload, Consumer<T> load) {
        // 如果最新配置为空，装载过的处理器全部卸载
        List<T> loadConfigHandlerList = getLoadConfigHandlerList();
        if (CollectionUtils.isEmpty(configHandlerList)) {
            if (!CollectionUtils.isEmpty(loadConfigHandlerList)) {
                loadConfigHandlerList.forEach(unload);
                setLoadConfigHandlerList(null);
            }
            return;
        }
        // 如果有最新配置，之前配置为空，全部装载
        if (CollectionUtils.isEmpty(loadConfigHandlerList)) {
            configHandlerList.forEach(load);
            return;
        }
        // 需删除配置处理器
        loadConfigHandlerList.stream().filter(h -> !configHandlerList.contains(h)).collect(Collectors.toList())
                .forEach(unload);
        // 需要添加的配置处理器
        configHandlerList.stream().filter(f -> !loadConfigHandlerList.contains(f)).forEach(load);
        // 记录最新配置处理器
        setLoadConfigHandlerList(configHandlerList);
    }

    /**
     * 刷新配置后加载拦截器
     */
    protected abstract void refresh();

}
