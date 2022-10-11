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

package com.github.yizzuide.milkomeda.hydrogen.core;

import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * AbstractHydrogenLoader
 *
 * @author yizzuide
 * @since 3.0.0
 * <br>
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
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;

        // 获取ApplicationContext后，刷新处理器列表
        refresh();
    }

    @EventListener
    public void configListener(DelegatingEnvironmentChangeEvent event) {
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
}
