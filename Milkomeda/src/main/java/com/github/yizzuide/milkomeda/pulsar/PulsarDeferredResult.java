package com.github.yizzuide.milkomeda.pulsar;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.UUID;

/**
 * PulsarDeferredResult
 * DeferredResult标识装配类
 *
 * @author yizzuide
 * @since  0.1.0
 * @version 0.2.8
 * Create at 2019/03/30 00:03
 */
@NoArgsConstructor
public class PulsarDeferredResult {
    /**
     * 唯一标识
     */
    @Getter
    private String deferredResultID;

    /**
     * 源DeferredResult
     */
    private DeferredResult<Object> deferredResult;

    /**
     * 引用Pulsar，用于监听设置回调
     */
    @Getter
    private Pulsar pulsar;

    PulsarDeferredResult(Pulsar pulsar) {
        this.pulsar = pulsar;
        // 设置默认标识
        this.setDeferredResultID(UUID.randomUUID().toString());
    }

    /**
     * 外部调用设置deferredResultID时，将自身放入Pulsar
     * @param deferredResultID 标识
     */
    public void setDeferredResultID(String deferredResultID) {
        // 如果在Pulsar中存放过，先拿出
        if (null != this.deferredResultID) {
            take();
        }
        this.deferredResultID = deferredResultID;
        pulsar.putDeferredResult(this);
    }

    /**
     * 获取DeferredResult，用于 Pulsar解包
     * @return DeferredResult
     */
    DeferredResult<Object> getDeferredResult() {
        return deferredResult;
    }

    /**
     * 封装进包装盒
     * @param deferredResult DeferredResult
     */
    void pack(DeferredResult<Object> deferredResult) {
        this.deferredResult = deferredResult;
    }

    /**
     * 从包装盒中取出DeferredResult
     * @return DeferredResult
     */
    public DeferredResult<Object> take() {
        return pulsar.takeDeferredResult(this.getDeferredResultID());
    }
}
