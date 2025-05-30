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

package com.github.yizzuide.milkomeda.atom.orbit;

import com.github.yizzuide.milkomeda.atom.AtomLock;
import com.github.yizzuide.milkomeda.atom.AtomProperties;
import com.github.yizzuide.milkomeda.orbit.AnnotationOrbitAdvisor;
import com.github.yizzuide.milkomeda.orbit.OrbitAdvisor;
import com.github.yizzuide.milkomeda.orbit.OrbitSource;
import com.github.yizzuide.milkomeda.orbit.OrbitSourceProvider;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

import java.util.Collections;
import java.util.List;

/**
 * Provide {@link OrbitAdvisor} for Orbit module to register advisor.
 *
 * @since 3.15.0
 * @version 3.20.0
 * @author yizzuide
 * <br>
 * Create at 2023/01/27 19:34
 */
@OrbitSourceProvider
public class AtomOrbitSource implements OrbitSource {
    @Override
    public List<OrbitAdvisor> createAdvisors(Environment environment) {
        // 根据配置文件是否加载来判断当前模块是否加载
        BindResult<AtomProperties> bindResult = Binder.get(environment).bind(AtomProperties.PREFIX, AtomProperties.class);
        if (!bindResult.isBound()) {
            return Collections.emptyList();
        }
        AnnotationOrbitAdvisor advisor = AnnotationOrbitAdvisor.forMethod(AtomLock.class, "atom", AtomOrbitAdvice.class, null);
        // set order after the Particle[1]
        advisor.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
        return Collections.singletonList(advisor);
    }
}
