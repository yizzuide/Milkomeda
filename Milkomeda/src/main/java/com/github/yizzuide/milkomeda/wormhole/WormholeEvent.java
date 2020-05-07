package com.github.yizzuide.milkomeda.wormhole;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

/**
 * DomainEvent
 * 领域事件
 *
 * @author yizzuide
 * @since 3.3.0
 * Create at 2020/05/05 14:13
 */
@Data
public class WormholeEvent<T> {
    /**
     * 领域事件还包含了唯一ID，但是该ID并不是实体层面的ID概念（如果是数据库存储，该字段通常为唯一索引），而主要用于事件追溯和日志
     */
    private String id;

    /**
     * 标签
     */
    private String tag;

    /**
     * 事件动作（内部会自动设置）
     */
    private String action;

    /**
     * 创建发生时间
     */
    private Date createTime;

    /**
     * 自定义包数据
     */
    private T data;

    public WormholeEvent() {
        this.id = UUID.randomUUID().toString().replaceAll("-", "");
        this.createTime = new Date();
    }

    public WormholeEvent(String tag, T data) {
        this();
        this.tag = tag;
        this.data = data;
    }
}
