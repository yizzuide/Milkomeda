/*
 * Copyright (c) 2024 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.comet.orbit;

import com.github.yizzuide.milkomeda.comet.core.CometX;
import com.github.yizzuide.milkomeda.orbit.AnnotationOrbitAdvisor;
import com.github.yizzuide.milkomeda.orbit.OrbitAdvisor;
import com.github.yizzuide.milkomeda.orbit.OrbitSource;
import com.github.yizzuide.milkomeda.orbit.OrbitSourceProvider;
import org.springframework.core.env.Environment;

import java.util.Collections;
import java.util.List;

/**
 * Provide {@link OrbitAdvisor} for Orbit module to register advisor.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2024/01/28 13:42
 */
@OrbitSourceProvider
public class CometOrbitSource implements OrbitSource {
    @Override
    public List<OrbitAdvisor> createAdvisors(Environment environment) {
        return Collections.singletonList(AnnotationOrbitAdvisor.forMethod(CometX.class, "cometX", CometXOrbitAdvice.class, null));
    }
}
