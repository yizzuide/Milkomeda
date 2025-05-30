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

package com.github.yizzuide.milkomeda.sirius.orbit;

import com.github.yizzuide.milkomeda.orbit.AnnotationOrbitAdvisor;
import com.github.yizzuide.milkomeda.orbit.OrbitAdvisor;
import com.github.yizzuide.milkomeda.orbit.OrbitSource;
import com.github.yizzuide.milkomeda.orbit.OrbitSourceProvider;
import com.github.yizzuide.milkomeda.sirius.Tenant;
import com.github.yizzuide.milkomeda.sirius.TenantProperties;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import java.util.Collections;
import java.util.List;

/**
 * Tenant implementation of {@link OrbitSource}.
 *
 * @since 3.20.0
 * @author yizzuide
 * Create at 2024/03/03 00:48
 */
@OrbitSourceProvider
public class TenantOrbitSource implements OrbitSource {
    @Override
    public List<OrbitAdvisor> createAdvisors(Environment environment) {
        BindResult<TenantProperties> bindResult = Binder.get(environment).bind(TenantProperties.PREFIX, TenantProperties.class);
        if (!bindResult.isBound()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(AnnotationOrbitAdvisor.forMethod(Tenant.class, "sirius_tenant", TenantOrbitAdvice.class, null));
    }
}
