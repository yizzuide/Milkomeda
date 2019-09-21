package com.github.yizzuide.milkomeda.comet;

import lombok.Data;

import java.util.Date;

/**
 * CometData
 *
 * @author yizzuide
 * @since 0.2.0
 * @version 1.12.0
 * Create at 2019/09/21 00:48
 */
@Data
public class CometData {
    /**
     * 日志记录名
     */
    private String name;
    /**
     * 日志描述
     * @deprecated deprecated at 1.12.0，use <code>name</code>.
     */
    private String description;
    /**
     * 记录数据 prototype（原型）的相应tag，用于请求分类，用于收集不同的类型数据
     */
    private String tag;
    /**
     * 服务器地址
     */
    private String host;
    /**
     * 微服务名
     */
    private String microName;
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
    private String requestData;
    /**
     * 响应时间
     */
    private Date responseTime;
    /**
     * 处理耗时
     */
    private String duration;
    /**
     * 响应数据
     */
    private String responseData;
    /**
     * 状态 1-成功；2-失败
     */
    private String status;
    /**
     * 错误信息
     */
    private String errorInfo;
    /**
     * 栈信息
     */
    private String traceStack;
}
