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

package com.github.yizzuide.milkomeda.demo.wormhole.infrastructure.repository;

import com.github.yizzuide.milkomeda.demo.wormhole.domain.model.Credit;
import com.github.yizzuide.milkomeda.wormhole.WormholeEvent;
import com.github.yizzuide.milkomeda.wormhole.WormholeEventTracker;
import com.github.yizzuide.milkomeda.wormhole.WormholeEventTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

/**
 * EventStreamRepository
 * 领域事件流仓储
 *
 * @author yizzuide
 * <br>
 * Create at 2020/05/05 15:42
 */
@Slf4j
@WormholeEventTracker
public class EventStreamRepository implements WormholeEventTrack<WormholeEvent<Credit>> {
    @Async
    @Override
    public void track(WormholeEvent<Credit> event) {
      log.info("存储事件到大数据流：{}", event);
    }
}
