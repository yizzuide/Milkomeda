/*
 * Copyright (c) 2022 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.light;

import io.netty.util.concurrent.FastThreadLocal;
import org.springframework.cloud.context.scope.GenericScope;
import org.springframework.cloud.context.scope.ScopeCache;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * LightThreadLocalScope
 * 自定义线程Scope，GenericScope内部有BeanLifecycleWrapper管理对bean的获取，并通过BeanFactoryPostProcessor注册自身
 *
 * @author yizzuide
 * @since 3.13.0
 * <br>
 * Create at 2022/03/10 23:41
 */
public class LightThreadLocalScope extends GenericScope implements Ordered {

    public LightThreadLocalScope(String name) {
        // 设置自定义Scope名
        super.setName(name);
        // 设置自定义Scope缓存
        super.setScopeCache(new LightScopeCache());
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 101;
    }

    @Override
    public void setId(String id) {
        super.setId(String.valueOf(Thread.currentThread().getId()));
    }

    @Override
    public String getConversationId() {
        return String.valueOf(Thread.currentThread().getId());
    }

    // 基于LightContext的Scope的缓存
    static class LightScopeCache implements ScopeCache {

        private final LightContext<String, Map<String, Object>> lightContext = new LightContext<>(new FastThreadLocal<Spot<String, Map<String, Object>>>(){
            // 覆盖初始化，防止获了为空
            @Override
            protected Spot<String, Map<String, Object>> initialValue() {
                Spot<String, Map<String, Object>> spot = new Spot<>();
                spot.setData(new HashMap<>());
                return spot;
            }
        });

        @Override
        public Object remove(String name) {
            return lightContext.get().getData().remove(name);
        }

        @Override
        public Collection<Object> clear() {
            Collection<Object> values = new ArrayList<>(lightContext.get().getData().values());
            lightContext.get().getData().clear();
            return values;
        }

        @Override
        public Object get(String name) {
            return lightContext.get().getData().get(name);
        }

        @Override
        public Object put(String name, Object value) {
            Object result = lightContext.get().getData().putIfAbsent(name, value);
            // 如果ThreadLocal里有，就用这里面的
            if (result != null) {
                return result;
            }
            return value;
        }
    }
}
