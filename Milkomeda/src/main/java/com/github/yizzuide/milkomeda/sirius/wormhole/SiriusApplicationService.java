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

package com.github.yizzuide.milkomeda.sirius.wormhole;

import com.github.yizzuide.milkomeda.wormhole.ApplicationService;
import com.github.yizzuide.milkomeda.wormhole.TransactionWorkBus;

/**
 * Mybatis plus extend of application service which provide {@link TransactionWorkBus}.
 *
 * @param <R> the repository type
 *
 * @since 3.15.0
 * @author yizzuide
 * Create at 2023/07/14 03:48
 */
public abstract class SiriusApplicationService<R> implements ApplicationService<R> {

    /**
     * Link {@link TransactionWorkBus} belong this application service.
     */
    private final TransactionWorkBus transactionWorkBus;

    public SiriusApplicationService() {
        transactionWorkBus = new SiriusTransactionWorkBus(false);
        transactionWorkBus.setApplicationService(this);
    }

    public TransactionWorkBus getTransactionWorkBus() {
        return transactionWorkBus;
    }

    /**
     * Change and enable batch insert with the primary key.
     * @param enable    true if batch insert with the primary key
     */
    public void setUseBatchInsertWithKey(boolean enable) {
        ((SiriusTransactionWorkBus) transactionWorkBus).setUseBatchInsertWithKey(enable);
    }
}
