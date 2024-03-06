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

package com.github.yizzuide.milkomeda.demo.quark;

import com.github.yizzuide.milkomeda.pulsar.PulsarHolder;
import com.github.yizzuide.milkomeda.quark.Quarks;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

/**
 * ChatController
 *
 * @author yizzuide
 * Create at 2023/08/19 14:25
 */
@Slf4j
@RequestMapping("/chat")
@RestController
public class ChatController {

    @RequestMapping("send")
    public String sendQuestion(String question) throws InterruptedException {
        // 模拟生成数据
        Random random = new Random();
        PulsarHolder.getPulsar().post(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(random.nextLong(0, 5) * 1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                MessageData data = new MessageData();
                data.setId(random.nextInt(0, 1000000));
                data.setUserId(1);
                data.setMsg(RandomStringUtils.randomAlphabetic(6));
                log.warn("put msg id: {}", data.getId());
                Quarks.bindProducer(data.getUserId().longValue(), "test").publishEventData(data);
            }
        });
        PulsarHolder.getPulsar().post(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(random.nextLong(0, 5) * 1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                MessageData data = new MessageData();
                data.setId(random.nextInt(0, 1000000));
                data.setUserId(2);
                data.setMsg(RandomStringUtils.randomAlphabetic(6));
                log.warn("put msg id: {}", data.getId());
                Quarks.bindProducer(data.getUserId().longValue(), "test").publishEventData(data);
            }
        });
        return "OK";
    }
}
