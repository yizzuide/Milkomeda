package com.github.yizzuide.milkomeda.demo.quark;

import com.github.yizzuide.milkomeda.quark.QuarkEvent;
import com.github.yizzuide.milkomeda.quark.QuarkEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Component;

/**
 * MessageEventHandler
 *
 * @author yizzuide
 * Create at 2023/08/19 14:34
 */
@Slf4j
@Component
public class MessageEventHandler extends QuarkEventHandler<MessageData> {
    @Override
    public void onEvent(QuarkEvent<MessageData> event, long sequence, boolean endOfBatch) throws Exception {
        Thread.sleep(RandomUtils.nextLong(0, 5) * 1000);
        log.info("event: {}, seq: {}, end: {}", event.getData().getId(), sequence, endOfBatch);
    }
}
