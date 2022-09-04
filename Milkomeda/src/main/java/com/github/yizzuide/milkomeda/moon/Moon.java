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

package com.github.yizzuide.milkomeda.moon;

import com.github.yizzuide.milkomeda.atom.AtomLock;
import com.github.yizzuide.milkomeda.atom.AtomLockType;
import com.github.yizzuide.milkomeda.light.LightCachePut;
import com.github.yizzuide.milkomeda.light.LightCacheable;
import com.github.yizzuide.milkomeda.universe.context.AopContextHolder;
import com.github.yizzuide.milkomeda.universe.extend.env.Environment;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Moon
 *
 * @author yizzuide
 * @since 2.2.0
 * @version 3.12.10
 * Create at 2019/12/31 18:13
 */
@Slf4j
@Data
public class Moon<T> {
    public static final String CACHE_NAME = "lightCacheMoon";

    /**
     * 缓存实例名
     */
    private String cacheName = CACHE_NAME;
    /**
     * 链表头指针
     */
    private MoonNode<T> header;
    /**
     * 链表记录指针
     */
    private MoonNode<T> pointer;
    /**
     * 链块连接指针
     */
    private MoonNode<T> next;
    /**
     * 链表长度
     */
    private int len;

    /**
     * 阶段列表
     */
    private List<T> phaseNames;

    /**
     * 阶段分配策略
     */
    private MoonStrategy moonStrategy;

    /**
     * 策略运行模式
     */
    private boolean mixinMode;

    /**
     * 设置并替换阶段
     * @param phaseNames    阶段列表
     */
    @SafeVarargs
    public final void set(T... phaseNames) {
        this.add(phaseNames);
    }

    /**
     * 添加阶段名
     * @param phaseNames    阶段列表
     */
    @SafeVarargs
    public final void add(T... phaseNames) {
        int len = phaseNames.length;
        if (len < 2) {
            throw new IllegalArgumentException("The phaseNames parameter must be greater than 2");
        }
        this.setPhaseNames(new ArrayList<>(Arrays.asList(phaseNames)));
        this.setLen(len);
        for (int i = 0; i < len; i++) {
            if (i == 0) {
                this.setHeader(new MoonNode<>());
                this.getHeader().setData(phaseNames[i]);
                this.setNext(this.getHeader());
                continue;
            }
            MoonNode<T> moonNode = new MoonNode<>();
            moonNode.setData(phaseNames[i]);
            this.getNext().setNext(moonNode);
            this.setNext(moonNode);
        }
        // 尾连首
        this.getNext().setNext(this.getHeader());
        // 指向首
        this.setPointer(this.getHeader());
        StringBuilder link = new StringBuilder();
        if (Environment.isShowLog()) {
            MoonNode<T> next = this.getHeader();
            do {
                link.append(" -> ");
                link.append(next.getData().toString());
            } while ((next = next.getNext()) != this.getHeader());
            log.info("load phases: {} <-", link);
        }
    }

    /**
     * 无序并发获得当前阶段类型（不支持分布式）
     * @return 阶段值
     */
    public T getCurrentPhase() {
        return this.getMoonStrategy().getCurrentPhase(this);
    }

    /**
     * 基于高性能lua获取（分布式并发安全）
     * @param key          缓存key，一个环对应一个key
     * @param prototype    Moon实例原型
     * @param <T>          阶段的类型
     * @return  当前环的当前阶段值
     */
    public static <T> T getPhase(String key, Moon<T> prototype) {
        if (prototype.isMixinMode()) {
            return prototype.getPhase(key);
        }
        return prototype.getMoonStrategy().getPhaseFast(key, prototype);
    }

    /**
     * 基于分布式锁获取，需要添加 <code>@EnableAspectJAutoProxy(exposeProxy=true)</code>（分布式并发安全）
     * @param key   缓存key，一个环对应一个key
     * @return  当前环的当前阶段值
     */
    @AtomLock(key = "#target.cacheName + '_' + #key", type = AtomLockType.NON_FAIR, waitTime = 60000)
    protected T getPhase(String key) {
        Moon<?> target = AopContextHolder.self(this.getClass());
        // 获取左手指月
        LeftHandPointer leftHandPointer = target.getLeftHandPointer(key);
        Integer p = leftHandPointer.getCurrent();
        T phase = this.getMoonStrategy().getPhase(key, p, this);
        // 开始左手指月，拔动月相
        target.pluckLeftHandPointer(key, leftHandPointer);
        return phase;
    }

    /**
     * 根据key拔动当前左手指月
     * @param key             缓存key
     * @param leftHandPointer 左手指月
     * @return LeftHandPointer
     */
    @SuppressWarnings({"UnusedReturnValue", "unused"})
    @LightCachePut(value = "#target.cacheName", keyPrefix = "moon:lhp-", key = "#key")
    protected LeftHandPointer pluckLeftHandPointer(String key, LeftHandPointer leftHandPointer) {
        return this.getMoonStrategy().pluck(this, leftHandPointer);
    }

    /**
     * 根据key获取当前左手指月（分布式环境下应该仅缓存到L2，自定义缓存配置通过 <code>setCacheName(String)</code>）
     * @param key 缓存key
     * @return LeftHandPointer
     */
    @SuppressWarnings("unused")
    @LightCacheable(value = "#target.cacheName", keyPrefix = "moon:lhp-", key = "#key")
    protected LeftHandPointer getLeftHandPointer(String key) {
        // 无法从缓存中获取时，创建新的左手指月
        return new LeftHandPointer();
    }
}
