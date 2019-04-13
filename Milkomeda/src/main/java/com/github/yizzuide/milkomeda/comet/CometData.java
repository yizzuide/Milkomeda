package com.github.yizzuide.milkomeda.comet;

import lombok.Data;

import java.util.Date;

/**
 * CometData
 *
 * @author yizzuide
 * @since 0.2.0
 * @version 0.2.1
 * Create at 2019/04/11 19:32
 */
@Data
public class CometData {
    /**
     * 请求编码
     */
    private String apiCode;
    /**
     * 请求描述
     */
    private String description;
    /**
     * 请求类型 1: 前台请求（默认） 2：第三方服务器推送
     */
    private String requestType;
    /**
     * 请求 URL
     */
    private String requestURL;
    /**
     * 请求路径
     */
    private String requestPath;
    /**
     * 请求方式
     */
    private String requestMethod;
    /**
     * 执行的方法
     */
    private String execMethod;
    /**
     * 请求时间
     */
    private Date requestTime;
    /**
     * 响应时间
     */
    private Date responseTime;
    /**
     * 服务器耗时
     */
    private String duration;
    /**
     * 服务器地址
     */
    private String host;
    /**
     * 请求 IP
     */
    private String requestIP;
    /**
     * 请求设备信息
     */
    private String deviceInfo;
    /**
     * 请求参数
     */
    private String requestData;
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
}
