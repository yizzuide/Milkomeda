package com.github.yizzuide.milkomeda.pulsar;

import lombok.Data;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * PulsarDeferredResult
 * DeferredResult的简单代理
 *
 * @author yizzuide
 * @since  0.1.0
 * @version 1.4.0
 * Create at 2019/03/30 00:03
 */
@Data
public class PulsarDeferredResult {
    /**
     * 唯一标识
     */
    private String deferredResultID;

    /**
     * 源DeferredResult
     */
    private DeferredResult<Object> deferredResult;

    /**
     * 返回包装的DeferredResult
     * @return DeferredResult
     */
    public DeferredResult<Object> value() {
        return deferredResult;
    }
}
