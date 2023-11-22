/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.comet.collector;

import com.github.yizzuide.milkomeda.comet.core.CometData;
import com.github.yizzuide.milkomeda.comet.core.CometRecorder;
import com.github.yizzuide.milkomeda.comet.core.EventDrivenWebCometData;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

/**
 * 日志收集器记录器
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 3.15.0
 * <br>
 * Create at 2019/11/13 19:18
 */
@Slf4j
public class CollectorRecorder implements CometRecorder {
    /**
     * 收集器工厂
     */
    private final CollectorFactory collectorFactory;

    public CollectorRecorder(CollectorFactory collectorFactory) {
        this.collectorFactory = collectorFactory;
    }

    @Override
    public void onRequest(CometData prototype, String tag, HttpServletRequest request, Object[] args) {
        try {
            // ignore event driven comet data
            if (prototype instanceof EventDrivenWebCometData) {
                return;
            }
            collectorFactory.get(tag).prepare(prototype);
        } catch (IllegalArgumentException e) {
            if (!e.getMessage().startsWith("type")) throw e;
        }
    }

    @Override
    public Object onReturn(CometData cometData, Object returnData) {
        try {
            // ignore event driven comet data
            if (cometData instanceof EventDrivenWebCometData) {
                return returnData;
            }
            collectorFactory.get(cometData.getTag()).onSuccess(cometData);
        } catch (IllegalArgumentException e) {
            if (!e.getMessage().startsWith("type")) throw e;
        }
        return returnData;
    }

    @Override
    public void onThrowing(CometData cometData, Exception e) {
        try {
            // ignore event driven comet data
            if (cometData instanceof EventDrivenWebCometData) {
                return;
            }
            collectorFactory.get(cometData.getTag()).onFailure(cometData);
        } catch (IllegalArgumentException ex) {
            if (!ex.getMessage().startsWith("type")) throw ex;
        }
    }
}
