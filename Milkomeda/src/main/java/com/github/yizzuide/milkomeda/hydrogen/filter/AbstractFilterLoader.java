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

package com.github.yizzuide.milkomeda.hydrogen.filter;

import com.github.yizzuide.milkomeda.hydrogen.core.AbstractHydrogenLoader;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AbstractFilterLoader
 *
 * @author yizzuide
 * @since 3.0.0
 * <br>
 * Create at 2020/04/03 01:01
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractFilterLoader<T> extends AbstractHydrogenLoader<T> implements FilterLoader {
    /**
     * Servlet上下文
     */
    private ServletContext servletContext;


    public List<Map<String, String>> inspect() {
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, ? extends FilterRegistration> filterRegistrations = servletContext.getFilterRegistrations();
        for (Map.Entry<String, ? extends FilterRegistration> entry : filterRegistrations.entrySet()) {
            Map<String, String> filterInfoMap = new HashMap<>();
            filterInfoMap.put("name",  entry.getKey());
            FilterRegistration filterRegistration = entry.getValue();
            filterInfoMap.put("class", filterRegistration.getClassName());
            filterInfoMap.put("urlPattern", filterRegistration.getUrlPatternMappings().toString());
            list.add(filterInfoMap);
        }
        return list;
    }
}
