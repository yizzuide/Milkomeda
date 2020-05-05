package com.github.yizzuide.milkomeda.wormhole;

/**
 * WormholeCallback
 * 领域事件回调
 *
 * @author yizzuide
 * @since 3.3.0
 * Create at 2020/05/05 14:47
 */
@FunctionalInterface
public interface WormholeCallback {
    <T> void callback(WormholeEvent<T> event, String action, Object result, Exception e);
}
