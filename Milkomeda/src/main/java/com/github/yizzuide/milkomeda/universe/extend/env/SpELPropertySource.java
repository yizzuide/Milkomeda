/*
 * Copyright (c) 2023 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.universe.extend.env;

import com.github.yizzuide.milkomeda.universe.engine.el.SimpleElParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Supported SpEL to YML value parse with PropertySource.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/01/02 19:12
 */
@Slf4j
public class SpELPropertySource extends PropertySource<Object> {

    public static final String EL_PROPERTY_SOURCE_NAME = "el";

    private static final String PREFIX = "el.";

    private static final String FUN_PARSE = "parse";

    public static final Map<String, Class<?>> TYPE_MAP = new HashMap<>();

    private final Object root;

    public SpELPropertySource(Object root) {
        super(EL_PROPERTY_SOURCE_NAME);
        this.root = root;
        if (!CollectionUtils.isEmpty(TYPE_MAP)) {
            return;
        }
        TYPE_MAP.put("INT", java.lang.Integer.class);
        TYPE_MAP.put("LONG", java.lang.Long.class);
        TYPE_MAP.put("BOOL", java.lang.Boolean.class);
        TYPE_MAP.put("STRING", java.lang.String.class);
        TYPE_MAP.put("DATE", java.util.Date.class);
        TYPE_MAP.put("OBJECT", java.lang.Object.class);
    }

    @Override
    public Object getProperty(String name) {
        if (!name.startsWith(PREFIX)) {
            return null;
        }
        Class<?> type = String.class;
        String fun = name.substring(PREFIX.length());
        if (fun.startsWith(FUN_PARSE)) {
            String condition = ConditionPropertySource.getRange(fun, FUN_PARSE);
            if (condition == null) {
                return null;
            }
            if (condition.contains(",")) {
                String[] parts = condition.split(",");
                String typeKey = parts[1].trim().toUpperCase();
                if (StringUtils.hasText(typeKey)) {
                    type = TYPE_MAP.get(typeKey);
                }
                return SimpleElParser.parse(parts[0], root, type);
            }
            return SimpleElParser.parse(condition, root, type);
        }
        return null;
    }

    public static void addToEnvironment(ConfigurableEnvironment environment) {
        environment.getPropertySources().addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new SpELPropertySource(environment));
        log.trace("SpELPropertySource add to Environment");
    }
}
