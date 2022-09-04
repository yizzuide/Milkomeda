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

package com.github.yizzuide.milkomeda.pillar;

import com.github.yizzuide.milkomeda.universe.context.SpringContext;
import com.github.yizzuide.milkomeda.universe.metadata.HandlerMetaData;
import com.github.yizzuide.milkomeda.util.Strings;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PillarContext
 *
 * @author yizzuide
 * @since 3.10.0
 * Create at 2020/07/02 17:15
 */
public class PillarEntryContext implements ApplicationListener<ContextRefreshedEvent> {

    public static final String ATTR_CODE = "code";

    private static Map<String, List<HandlerMetaData>> pillarEntryMap = new HashMap<>();

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        pillarEntryMap = SpringContext.getHandlerMetaData(PillarEntryHandler.class, PillarEntryPoint.class, (annotation, handlerAnnotation, metaData) -> {
            PillarEntryHandler pillarEntryHandler = (PillarEntryHandler) handlerAnnotation;
            PillarEntryPoint pillarEntryPoint = (PillarEntryPoint) annotation;
            String tag = pillarEntryHandler.tag();
            if (Strings.isEmpty(tag)) {
                tag = pillarEntryPoint.tag();
            }
            // 设置其它属性方法的值
            Map<String, Object> attrs = new HashMap<>(2);
            attrs.put(ATTR_CODE, pillarEntryPoint.code());
            metaData.setAttributes(attrs);
            return tag;
        }, false);
    }

    static Map<String, List<HandlerMetaData>> getPillarEntryMap() {
        return pillarEntryMap;
    }
}
