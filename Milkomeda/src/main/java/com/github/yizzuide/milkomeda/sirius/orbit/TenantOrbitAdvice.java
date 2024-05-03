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

import com.github.yizzuide.milkomeda.orbit.OrbitAdvice;
import com.github.yizzuide.milkomeda.orbit.OrbitInvocation;
import com.github.yizzuide.milkomeda.sirius.SiriusHolder;
import com.github.yizzuide.milkomeda.sirius.Tenant;
import com.github.yizzuide.milkomeda.sirius.TenantData;
import com.github.yizzuide.milkomeda.universe.engine.el.ELContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

/**
 * Tenant implementation of {@link OrbitAdvice}.
 *
 * @since 3.20.0
 * @author yizzuide
 * Create at 2024/03/03 00:51
 */
public class TenantOrbitAdvice implements OrbitAdvice {
    @Override
    public Object invoke(OrbitInvocation invocation) throws Throwable {
        Tenant tenant = AnnotationUtils.findAnnotation(invocation.getMethod(), Tenant.class);
        if (tenant != null) {
            String idValue = tenant.idValue();
            if (StringUtils.hasText(idValue)) {
                idValue = ELContext.getValue(invocation.getPjp(), idValue);
            }
            TenantData tenantData = TenantData.builder()
                    .ignored(tenant.ignored())
                    .idColumn(tenant.idColumn())
                    .idValue(idValue)
                    .build();
            SiriusHolder.setTenantData(tenantData);
        }
        try {
            return invocation.proceed();
        } finally {
            if (tenant != null) {
                SiriusHolder.clear();
            }
        }
    }
}
