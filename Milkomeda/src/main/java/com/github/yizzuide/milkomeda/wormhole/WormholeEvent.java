/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.wormhole;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.EventObject;
import java.util.UUID;

/**
 * 领域事件
 *
 * @author yizzuide
 * @since 3.3.0
 * @version 3.13.0
 * <br>
 * Create at 2020/05/05 14:13
 */
@ToString(callSuper = true)
public class WormholeEvent<T> extends EventObject {
    private static final long serialVersionUID = 8724358110950841351L;
    /**
     * 领域事件还包含了唯一ID，但是该ID并不是实体层面的ID概念（如果是数据库存储，该字段通常为唯一索引），而主要用于事件追溯和日志
     */
    @Setter @Getter
    private String id;

    /**
     * 标签，多个以","分隔
     */
    @Setter @Getter
    private String tag;

    /**
     * 事件动作（内部会自动设置）
     */
    @Setter @Getter
    private String action;

    /**
     * 创建发生时间
     */
    @Setter @Getter
    private Date createTime;

    /**
     * 自定义包数据
     */
    @Setter @Getter
    private T data;

    /**
     * Create with event source.
     * @param source event source
     */
    public WormholeEvent(Object source) {
        super(source);
        this.id = UUID.randomUUID().toString().replaceAll("-", "");
        this.createTime = new Date();
    }

    /**
     * Common create with event source, tag, data.
     * @param source    event source
     * @param tag       event tag
     * @param data      event data
     */
    public WormholeEvent(Object source, String tag, T data) {
        this(source);
        this.tag = tag;
        this.data = data;
    }
}
