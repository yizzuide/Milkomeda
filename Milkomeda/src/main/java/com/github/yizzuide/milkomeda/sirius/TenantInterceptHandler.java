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

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.github.yizzuide.milkomeda.universe.extend.env.SpELPropertySource;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Custom tenant intercept handler.
 *
 * @since 3.20.0
 * @author yizzuide
 * Create at 2024/01/10 14:46
 */
public class TenantInterceptHandler implements TenantLineHandler {

    private final TenantProperties tenantProperties;

    private final Set<String> ignoreTables;

    public TenantInterceptHandler(TenantProperties tenantProperties) {
        this.tenantProperties = tenantProperties;
        if (CollectionUtils.isEmpty(tenantProperties.getTempIgnoreTables())) {
            ignoreTables = new HashSet<>(tenantProperties.getIgnoreTables());
            return;
        }
        ignoreTables = new CopyOnWriteArraySet<>(tenantProperties.getIgnoreTables());
        ignoreTables.addAll(tenantProperties.getTempIgnoreTables());
    }

    @Override
    public String getTenantIdColumn() {
        if (SiriusHolder.getTenantData() != null && StringUtils.hasText(SiriusHolder.getTenantData().getIdColumn())) {
            return SiriusHolder.getTenantData().getIdColumn();
        }
        return this.tenantProperties.getTenantIdName();
    }

    @Override
    public Expression getTenantId() {
        if (SiriusHolder.getTenantData() != null && StringUtils.hasText(SiriusHolder.getTenantData().getIdValue())) {
            return new LongValue(SiriusHolder.getTenantData().getIdValue());
        }
        return new LongValue((Long) SpELPropertySource.parseElFun(tenantProperties.getTenantIdExpression()));
    }

    @Override
    public boolean ignoreTable(String tableName) {
        if (SiriusHolder.getTenantData() != null && SiriusHolder.getTenantData().isIgnored()) {
            return true;
        }
        return ignoreTables.stream().anyMatch(table -> table.equals(tableName.toLowerCase()) || table.equals(tableName.toUpperCase()));
    }

    /**
     * Remove ignore tables from temp set.
     */
    public void removeTempIgnoreTables() {
        ignoreTables.removeAll(this.tenantProperties.getTempIgnoreTables());
    }
}
