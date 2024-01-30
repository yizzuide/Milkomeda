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

package com.github.yizzuide.milkomeda.comet.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Basic comet log data.
 *
 * @author yizzuide
 * @since 0.2.0
 * @version 4.0.0
 * <br>
 * Create at 2019/09/21 00:48
 */
@Data
@ToString(exclude = {"failure", "attachment", "intentData"})
public class CometData implements Serializable {
    @Serial
    private static final long serialVersionUID = -8296355140769902642L;

    /**
     * 记录名
     */
    private String name;

    /**
     * 唯一标识码
     */
    private String code;

    /**
     * 分类标签
     */
    private String tag;

    /**
     * 类名
     */
    private String clazzName;

    /**
     * 执行的方法
     */
    private String execMethod;

    /**
     * 请求时间
     */
    private Date requestTime;

    /**
     * 方法参数
     */
    private Object requestData;

    /**
     * 响应时间
     */
    private Date responseTime;

    /**
     * 处理耗时
     */
    private Long duration;

    /**
     * 响应数据
     */
    private Object responseData;

    /**
     * 处理状态码
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorInfo;

    /**
     * 栈信息
     */
    private String traceStack;

    /**
     * 业务端有异常时设置失败，日志收集器将走失败流程
     * @since 3.0.5
     */
    @JsonIgnore
    private transient Exception failure;

    /**
     * 跟踪附件，用于设置日志记录实体
     */
    @JsonIgnore
    private transient Object attachment;

    /**
     * 业务端传递给日志收集器的意图数据
     * @since 3.0.5
     */
    @JsonIgnore
    private transient Object intentData;
}
