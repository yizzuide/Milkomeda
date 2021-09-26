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

package com.github.yizzuide.milkomeda.universe.env;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.StringUtils;

/**
 * ConditionPropertySource
 *
 * @author yizzuide
 * @version 3.2.1
 * Create at 2020/04/29 15:30
 */
@Slf4j
public class ConditionPropertySource extends PropertySource<Object> {

    public static final String CONDITION_PROPERTY_SOURCE_NAME = "condition";

    private static final String PREFIX = "condition.";

    public ConditionPropertySource() {
        super(CONDITION_PROPERTY_SOURCE_NAME);
    }

    @Override
    public Object getProperty(String name) {
        if (!name.startsWith(PREFIX)) {
            return null;
        }
        return getConditionValue(name.substring(PREFIX.length()));
    }

    private Object getConditionValue(String type) {
        String range = getRange(type, "equals");
        if (range != null) {
            return getBoolValue(range);
        }
        range = getRange(type, "diff");
        if (range != null) {
            return !getBoolValue(range);
        }
        return null;
    }

    private boolean getBoolValue(String range) {
        range = StringUtils.trimAllWhitespace(range);
        String[] parts = StringUtils.commaDelimitedListToStringArray(range);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Condition.bool set args error.");
        }
        return parts[0].equals(parts[1]);
    }

    private String getRange(String type, String prefix) {
        if (type.startsWith(prefix)) {
            int startIndex = prefix.length() + 1;
            if (type.length() > startIndex) {
                return type.substring(startIndex, type.length() - 1);
            }
        }
        return null;
    }

    public static void addToEnvironment(ConfigurableEnvironment environment) {
        environment.getPropertySources().addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new ConditionPropertySource());
        log.trace("CollectionsPropertySource add to Environment");
    }
}
