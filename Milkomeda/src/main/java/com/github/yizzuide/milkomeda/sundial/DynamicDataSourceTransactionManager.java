/*
 * Copyright (c) 2025 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.sundial;

import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.Serial;

/**
 * A custom transaction manager, it fixed {@link Sundial} and {@link Transactional} don't work together.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/06/16 15:44
 */
public class DynamicDataSourceTransactionManager extends DataSourceTransactionManager {

    @Serial
    private static final long serialVersionUID = 8571896907127979281L;

    private final AbstractRoutingDataSource routingDataSource;

    public DynamicDataSourceTransactionManager(AbstractRoutingDataSource routingDataSource) {
        super(routingDataSource);
        this.routingDataSource = routingDataSource;
    }

    @Override
    protected void doBegin(@NotNull Object transaction, @NotNull TransactionDefinition definition) {
        // 在事务开始前切换数据源
        switchDataSourceIfNeeded();
        super.doBegin(transaction, definition);
    }

    private void switchDataSourceIfNeeded() {
        String lookupKey = SundialHolder.getDataSourceType();
        if (lookupKey != null) {
            // 获取当前已绑定的数据源
            DataSource currentDataSource = super.getDataSource();

            // 获取当前线程指定的目标数据源
            DataSource targetDataSource = routingDataSource.getResolvedDataSources().get(lookupKey);

            // 检查是否已绑定正确数据源
            if (targetDataSource != null && targetDataSource != currentDataSource) {
                setDataSource(targetDataSource);
            }
        }
    }

    public void setDataSource(DataSource dataSource) {
        // 利用父类方法进行实际切换
        if (super.getDataSource() != dataSource) {
            super.setDataSource(dataSource);
        }
    }

    @Override
    protected void doCleanupAfterCompletion(@NotNull Object transaction) {
        try {
            super.doCleanupAfterCompletion(transaction);
        } finally {
            // 清理线程上下文
            SundialHolder.clearDataSourceType();
        }
    }
}
