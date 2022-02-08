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

package com.github.yizzuide.milkomeda.ice;

import com.github.yizzuide.milkomeda.moon.Moon;
import com.github.yizzuide.milkomeda.moon.PeriodicMoonStrategy;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * DelegatingDelayJobHandler
 * 代理对多个延迟桶的调度处理
 *
 * @author yizzuide
 * @since 3.8.0
 * @version 3.12.0
 * Create at 2020/06/11 11:24
 */
public class DelegatingDelayJobHandler implements Runnable, InitializingBean {

    @Autowired
    private JobPool jobPool;

    @Autowired
    private DelayBucket delayBucket;

    @Autowired
    private ReadyQueue readyQueue;

    @Autowired
    private DeadQueue deadQueue;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private IceProperties props;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private StringRedisTemplate redisTemplate;

    // 使用Moon来轮询延迟桶
    private Moon<DelayJobHandler> iceDelayBucketMoon;

    private String delayBucketKey = "ice-delay-bucket";

    @Override
    public void run() {
        DelayJobHandler delayJobHandler = Moon.getPhase(delayBucketKey, iceDelayBucketMoon);
        delayJobHandler.run();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void afterPropertiesSet() throws Exception {
        if (!IceProperties.DEFAULT_INSTANCE_NAME.equals(props.getInstanceName())) {
            this.delayBucketKey = "ice-delay-bucket:" + props.getInstanceName();
        }
        // 延迟桶处理器
        List<DelayJobHandler> delayJobHandlers = new ArrayList<>();
        for (int i = 0; i < props.getDelayBucketCount(); i++) {
            // 注册为bean，让其可以接收Spring事件
            DelayJobHandler delayJobHandler = WebContext.registerBean((ConfigurableApplicationContext) ApplicationContextHolder.get(), "delayJobHandler" + i, DelayJobHandler.class);
            delayJobHandler.fill(redisTemplate, jobPool, delayBucket, readyQueue, deadQueue, i, props);
            delayJobHandlers.add(delayJobHandler);
        }
        Moon<DelayJobHandler> moon = WebContext.registerBean((ConfigurableApplicationContext) ApplicationContextHolder.get(), "iceDelayBucketMoon", Moon.class);
        // 使用lua方式
        moon.setMixinMode(false);
        PeriodicMoonStrategy strategy = new PeriodicMoonStrategy();
        String luaScript = strategy.loadLuaScript();
        strategy.setLuaScript(luaScript);
        moon.setMoonStrategy(strategy);
        moon.add(delayJobHandlers.toArray(new DelayJobHandler[0]));
        iceDelayBucketMoon = moon;
    }

    @EventListener
    public void onApplicationEvent(IceInstanceChangeEvent event) {
        String instanceName = event.getSource().toString();
        this.delayBucketKey = "ice-delay-bucket:" + instanceName;
    }
}
