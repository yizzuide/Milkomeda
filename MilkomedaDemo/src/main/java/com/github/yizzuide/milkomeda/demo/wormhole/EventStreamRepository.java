package com.github.yizzuide.milkomeda.demo.wormhole;

import com.github.yizzuide.milkomeda.wormhole.WormholeEvent;
import com.github.yizzuide.milkomeda.wormhole.WormholeEventTracker;
import com.github.yizzuide.milkomeda.wormhole.WormholeEventTrack;
import lombok.extern.slf4j.Slf4j;

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
public class EventStreamRepository implements WormholeEventTrack<WormholeEvent<?>> {
    @Override
    public void track(WormholeEvent<?> event) {
      log.info("存储事件到大数据流：{}", event);
    }
}
