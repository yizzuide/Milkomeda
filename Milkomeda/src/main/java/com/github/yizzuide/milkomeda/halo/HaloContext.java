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

package com.github.yizzuide.milkomeda.halo;

import com.github.yizzuide.milkomeda.universe.context.SpringContext;
import com.github.yizzuide.milkomeda.universe.metadata.HandlerMetaData;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HaloContext
 * 上下文信息
 *
 * @author yizzuide
 * @since 2.5.0
 * @version 3.11.1
 * <br>
 * Create at 2020/01/30 22:40
 * @see org.springframework.context.support.AbstractApplicationContext#refresh()
 */
public class HaloContext implements ApplicationListener<ContextRefreshedEvent> {
    /**
     * 监听类型的方法属性名
     */
    public static final String ATTR_TYPE = "type";

    private static Map<String, List<HandlerMetaData>> tableNameMap = new HashMap<>();

    private static final Map<String, List<HandlerMetaData>> preTableNameMap = new HashMap<>();

    private static final Map<String, List<HandlerMetaData>> postTableNameMap = new HashMap<>();

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        tableNameMap = SpringContext.getHandlerMetaData(HaloHandler.class, HaloListener.class, (annotation, handlerAnnotation, metaData) -> {
            HaloListener haloListener = (HaloListener) annotation;
            // 设置其它属性方法的值
            Map<String, Object> attrs = new HashMap<>(4);
            attrs.put(ATTR_TYPE, haloListener.type());
            metaData.setAttributes(attrs);
            String value = haloListener.value();
            cacheWithType(haloListener.type() == HaloType.PRE ? preTableNameMap : postTableNameMap, value, metaData);
            return value;
        }, false);
    }

    private void cacheWithType(Map<String, List<HandlerMetaData>> map, String tableName, HandlerMetaData metaData) {
        if (map.get(tableName) == null) {
            List<HandlerMetaData> handlers = new ArrayList<>();
            handlers.add(metaData);
            map.put(tableName, handlers);
        } else {
            map.get(tableName).add(metaData);
        }
    }

    static Map<String, List<HandlerMetaData>> getTableNameMap() {
        return tableNameMap;
    }

    static Map<String, List<HandlerMetaData>> getPostTableNameMap() {
        return postTableNameMap;
    }

    static Map<String, List<HandlerMetaData>> getPreTableNameMap() {
        return preTableNameMap;
    }
}
