package com.github.yizzuide.milkomeda.particle;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * BarrierLimiter
 * 组合限制器
 *
 * 用于组装多个限制处理器，也能实现复合链串：限制器 + 组合限制器 + ...
 *
 * @author yizzuide
 * @since 1.5.0
 * @version 3.0.0
 * Create at 2019/05/31 11:25
 */
public class BarrierLimiter extends LimitHandler {
    /**
     * 拦截链头
     */
    private LimitHandler head;

    /**
     * 限制器名链（作为YML配置使用）
     */
    @Setter @Getter
    private List<String> chain;

    /**
     * 添加限制处理器
     * @param limitHandlerList 限制处理器集合
     */
    public void addLimitHandlerList(List<LimitHandler> limitHandlerList) {
        for (LimitHandler handler : limitHandlerList) {
            if (head == null) {
                head = handler;
                next = head;
                continue;
            }
            next.setNext(handler);
            next = next.getNext();
        }
    }

    @Override
    public <R> R limit(String key, long expire, Process<R> process) throws Throwable {
        return head.limit(key, expire, process);
    }
}
