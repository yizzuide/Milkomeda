package com.github.yizzuide.milkomeda.particle;

import java.util.List;

/**
 * BarrierLimiter
 * 组合限制器
 *
 * 用于组装多个限制处理器，也能实现复合链串：限制器 + 组合限制器 + ...
 *
 * @author yizzuide
 * @since 1.5.0
 * Create at 2019/05/31 11:25
 */
public class BarrierLimiter extends LimitHandler {
    /**
     * 拦截链头
     */
    private LimitHandler limitHandler;

    /**
     * 添加限制处理器
     * @param limitHandlerList 限制处理器集合
     */
    public void addLimitHandlerList(List<LimitHandler> limitHandlerList) {
        for (LimitHandler handler : limitHandlerList) {
            if (limitHandler == null) {
                limitHandler = handler;
                next = limitHandler;
                continue;
            }
            next.setNext(handler);
            next = next.getNext();
        }
    }

    @Override
    public <R> R limit(String key, long expire, Process<R> process) throws Throwable {
        return limitHandler.limit(key, expire, process);
    }
}
