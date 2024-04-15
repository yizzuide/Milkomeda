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

package com.github.yizzuide.milkomeda.sirius;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.Set;

/**
 * Tenant config.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2024/01/10 14:48
 */
@Data
@ConfigurationProperties(prefix = TenantProperties.PREFIX)
public class TenantProperties {

    public static final String PREFIX = "milkomeda.sirius.tenant";

    /**
     * Enable tenant intercept mode.
     */
    private boolean enable = false;

    /**
     * Set the tenant id name.
     */
    private String tenantIdName = "tenant_id";

    /**
     * Set the tenant id expression（SpringEL）, the parse result type must be {@link Long}.
     */
    private String tenantIdExpression;

    /**
     *  Table that needs to be ignored temporarily.
     */
    private Set<String> tempIgnoreTables = Collections.emptySet();

    /**
     * Ignore tables.
     */
    private Set<String> ignoreTables = Collections.emptySet();
}
