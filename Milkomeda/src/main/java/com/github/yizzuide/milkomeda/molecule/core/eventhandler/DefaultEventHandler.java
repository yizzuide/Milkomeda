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

package com.github.yizzuide.milkomeda.molecule.core.eventhandler;

import com.github.yizzuide.milkomeda.molecule.MoleculeContext;
import com.github.yizzuide.milkomeda.molecule.core.agg.AbstractAggregateRoot;
import com.github.yizzuide.milkomeda.molecule.core.event.AwakePerformAggregateEvent;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;

/**
 * The default event handler for handle {@link AwakePerformAggregateEvent}.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/06/11 18:14
 */
public class DefaultEventHandler {
    @Transactional(rollbackFor = Throwable.class)
    @EventListener
    public void handle(AwakePerformAggregateEvent ignore) {
        if(MoleculeContext.getDomainEventBus().hasHangingType(AbstractAggregateRoot.class)) {
            MoleculeContext.getDomainEventBus().publish();
        }
    }
}
