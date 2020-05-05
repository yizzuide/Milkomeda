package com.github.yizzuide.milkomeda.wormhole;

/**
 * WormholeEventTrack
 * 领域事件流跟踪
 *
 * @author yizzuide
 * @since 3.3.0
 * @see WormholeEventTracker
 * Create at 2020/05/05 14:48
 */
@FunctionalInterface
public interface WormholeEventTrack {
    void track(WormholeEvent<?> event);
}
