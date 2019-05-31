package com.github.yizzuide.milkomeda.particle;

import java.util.List;

/**
 * BarrierLimiter
 * 拦截链限制器
 *
 * 用于组装限制处理器，也能实现链串
 *
 * @author yizzuide
 * @since 1.5.0
 * Create at 2019/05/31 11:25
 */
public class BarrierLimiter extends LimitHandler {
    /**
     * 拦截链头
     */
    private LimitHandler header;

    /**
     * 添加限制处理器
     * @param limitHandlerList 限制处理器集合
     */
    public void addLimitHandlerList(List<LimitHandler> limitHandlerList) {
        for (LimitHandler handler : limitHandlerList) {
            if (header == null) {
                header = handler;
                next = header;
                continue;
            }
            next.setNext(handler);
            next = next.getNext();
        }
    }

    @Override
    public <R> R limit(String key, long expire, Process<R> process) throws Throwable {
        return header.limit(key, expire, process);
    }
}
