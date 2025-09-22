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

package com.github.yizzuide.milkomeda.demo.quark;

import com.github.yizzuide.milkomeda.quark.Quark;
import com.github.yizzuide.milkomeda.quark.QuarkEvent;
import com.github.yizzuide.milkomeda.quark.QuarkEventHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/**
 * MessageEvent3Handler
 *
 * @author yizzuide
 * Create at 2025/09/22 13:59
 */
@Quark(topic = "test", name = "h3")
@Slf4j
public class MessageEvent3Handler extends QuarkEventHandler<MessageData> {
    @Override
    public void onEvent(QuarkEvent<MessageData> event, long sequence, boolean endOfBatch) throws Exception {
        Thread.sleep(new Random().nextLong(0, 5) * 1000);
        log.info("event3: {}, seq: {}, end: {}", event.getData().getId(), sequence, endOfBatch);
    }
}
