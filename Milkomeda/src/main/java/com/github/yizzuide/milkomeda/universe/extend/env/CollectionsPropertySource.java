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

package com.github.yizzuide.milkomeda.universe.extend.env;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Collections;
import java.util.Date;

/**
 * CollectionsPropertySource
 *
 * @author yizzuide
 * @since 3.0.1
 * @see org.springframework.boot.env.RandomValuePropertySource#addToEnvironment(org.springframework.core.env.ConfigurableEnvironment)
 * @see org.springframework.core.env.AbstractEnvironment#getProperty(java.lang.String)
 * @see org.springframework.core.env.AbstractPropertyResolver#getProperty(java.lang.String)
 * @see org.springframework.core.env.PropertySourcesPropertyResolver#getProperty(java.lang.String, java.lang.Class)
 * <br>
 * Create at 2020/04/11 10:38
 */
@Slf4j
public class CollectionsPropertySource extends PropertySource<Object> {

    public static final String COLLECTIONS_PROPERTY_SOURCE_NAME = "collections";

    public static final String COLLECTIONS_EMPTY_MAP = "{}";

    public static final String COLLECTIONS_EMPTY_LIST = "[]";

    public static final String COLLECTIONS_OBJECT_DATE = "{date}";

    public static final String COLLECTIONS_OBJECT_MILLS = "{mills}";

    private static final String PREFIX = "collections.";

    public CollectionsPropertySource() {
        super(COLLECTIONS_PROPERTY_SOURCE_NAME);
    }

    @Override
    public Object getProperty(String name) {
        if (!name.startsWith(PREFIX)) {
            return null;
        }
        return getValue(name.substring(PREFIX.length()));
    }

    public Object getValue(String type) {
        if ("emptyMap".equals(type)) {
            return COLLECTIONS_EMPTY_MAP;
        }
        if ("emptyList".equals(type)) {
            return COLLECTIONS_EMPTY_LIST;
        }
        if ("dateObject".equals(type)) {
            return COLLECTIONS_OBJECT_DATE;
        }
        if ("millsObject".equals(type)) {
            return COLLECTIONS_OBJECT_MILLS;
        }
        return null;
    }

    /**
     * Get empty object with token
     * @param psValue token string
     * @return  empty object
     * @since 3.14.0
     */
    public static Object of(Object psValue) {
        if (psValue == null) {
            return null;
        }
        if (psValue.equals(COLLECTIONS_EMPTY_MAP)) {
            return Collections.emptyMap();
        }
        if (psValue.equals(COLLECTIONS_EMPTY_LIST)) {
            return Collections.emptyList();
        }
        if (psValue.equals(COLLECTIONS_OBJECT_DATE)) {
            return new Date();
        }
        if (psValue.equals(COLLECTIONS_OBJECT_MILLS)) {
            return System.currentTimeMillis();
        }
        return psValue;
    }

    public static void addToEnvironment(ConfigurableEnvironment environment) {
        // 添加在SystemEnvironmentPropertySource后面，而SpringCloud（bootstrap.yml，比application.yml优化级高）和SpringBoot（application.yml）环境里的配置可以读取
        environment.getPropertySources().addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new CollectionsPropertySource());
        log.trace("CollectionsPropertySource add to Environment");
    }
}
