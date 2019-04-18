package com.github.yizzuide.milkomeda.pulsar;

import lombok.Data;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * PulsarDeferredResult
 * DeferredResult标识装配类
 *
 * @author yizzuide
 * @since  0.1.0
 * @version 0.2.7
 * Create at 2019/03/30 00:03
 */
@Data
public class PulsarDeferredResult {
    /**
     * 唯一标识
     */
    private volatile String deferredResultID;
    /**
     * 源DeferredResult
     */
    private DeferredResult<Object> deferredResult;
}
