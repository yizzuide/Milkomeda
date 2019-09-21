package com.github.yizzuide.milkomeda.comet;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * CometData
 *
 * @author yizzuide
 * @since 1.12.0
 * Create at 2019/04/11 19:32
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WebCometData extends CometData {
    /**
     * 接口请求序号
     */
    private String apiCode;
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
     * 请求参数
     */
    private String requestParams;
    /**
     * 请求 IP
     */
    private String requestIP;
    /**
     * 请求设备信息
     */
    private String deviceInfo;
}
