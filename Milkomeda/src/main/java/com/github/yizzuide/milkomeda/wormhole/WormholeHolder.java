package com.github.yizzuide.milkomeda.wormhole;

/**
 * WormholeHolder
 *
 * @author yizzuide
 * @since 3.3.0
 * Create at 2020/05/05 14:52
 */
public class WormholeHolder {

    private static WormholeEventBus eventBus;

    static void setEventBus(WormholeEventBus eventBus) {
        WormholeHolder.eventBus = eventBus;
    }

    /**
     * 获取领域事件总线
     * @return  WormholeEventBus
     */
    public static WormholeEventBus getEventBus() {
        return eventBus;
    }
}
