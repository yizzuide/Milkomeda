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

package com.github.yizzuide.milkomeda.sundial.orbit;

import com.github.yizzuide.milkomeda.orbit.AnnotationOrbitAdvisor;
import com.github.yizzuide.milkomeda.orbit.OrbitAdvisor;
import com.github.yizzuide.milkomeda.orbit.OrbitSource;
import com.github.yizzuide.milkomeda.orbit.OrbitSourceProvider;
import com.github.yizzuide.milkomeda.sundial.Sundial;
import com.github.yizzuide.milkomeda.sundial.SundialProperties;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import java.util.Collections;
import java.util.List;

/**
 * Register Orbit node from yml config with annotation.
 *
 * @since 3.15.0
 * @version 3.20.0
 * @author yizzuide
 * <br>
 * Create at 2023/04/15 02:45
 */
@OrbitSourceProvider
public class AnnotationOrbitSource implements OrbitSource {
    @Override
    public List<OrbitAdvisor> createAdvisors(Environment environment) {
        // 根据配置文件是否加载来判断当前模块是否加载
        BindResult<SundialProperties> bindResult = Binder.get(environment).bind(SundialProperties.PREFIX, SundialProperties.class);
        if (!bindResult.isBound()) {
            return Collections.emptyList();
        }
        AnnotationOrbitAdvisor annotationOrbitAdvisor = new AnnotationOrbitAdvisor(Sundial.class, Sundial.class, "sundial", AnnotationDataSourceOrbitAdvice.class, null);
        return Collections.singletonList(annotationOrbitAdvisor);
    }
}
