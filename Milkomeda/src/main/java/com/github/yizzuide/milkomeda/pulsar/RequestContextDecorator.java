package com.github.yizzuide.milkomeda.pulsar;

import org.springframework.core.task.TaskDecorator;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * RequestContextDecorator
 * 请求上正文装饰器
 *
 * @author yizzuide
 * @since  1.13.11
 * Create at 2019/03/29 10:36
 */
public class RequestContextDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable) {
        RequestAttributes context;
        try {
            context = RequestContextHolder.currentRequestAttributes();
        } catch (Exception e) {
            return runnable;
        }
        RequestAttributes finalContext = context;
        return () -> {
            try {
                RequestContextHolder.setRequestAttributes(finalContext);
                runnable.run();
            } finally {
                RequestContextHolder.resetRequestAttributes();
            }
        };
    }
}