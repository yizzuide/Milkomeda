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

package com.github.yizzuide.milkomeda.demo.wormhole.domain.service;

import com.github.yizzuide.milkomeda.demo.wormhole.domain.aciton.Actions;
import com.github.yizzuide.milkomeda.demo.wormhole.domain.model.Credit;
import com.github.yizzuide.milkomeda.wormhole.WormholeAction;
import com.github.yizzuide.milkomeda.wormhole.WormholeEvent;
import com.github.yizzuide.milkomeda.wormhole.WormholeEventHandler;
import com.github.yizzuide.milkomeda.wormhole.WormholeTransactionHangType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 额度领域服务
 *
 * @author yizzuide
 * <br>
 * Create at 2020/05/05 15:39
 */
@Slf4j
@Service
@WormholeEventHandler
public class QuotaService {

    @Async
    @WormholeAction(value = Actions.AUDIT_SUCCESS, transactionHang = WormholeTransactionHangType.AFTER_COMMIT)
    public void onEvent(WormholeEvent<Credit> event) {
        // 调用信用聚合根，更新额度
        event.getData().updateQuota(20000L);
        log.info("审核后修改用户额度: {}", event);

        // 调用其它实体聚合...

    }
}
