package com.github.yizzuide.milkomeda.echo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * EchoProperties
 *
 * @author yizzuide
 * @since 1.13.3
 * Create at 2019/10/23 20:53
 */
@Data
@ConfigurationProperties("milkomeda.echo")
public class EchoProperties {
    /**
     * 连接池最大连接数，默认200
     */
    int poolMaxSize = 200;
    /**
     * 每个路由的并发量，默认50
     */
    int defaultMaxPerRoute = 50;
    /**
     * 连接超时ms，默认5000
     */
    int connectTimeout = 5000;
    /**
     * 数据读取超时ms，默认5000
     */
    int readTimeout = 5000;
    /**
     * 从池中获取请求连接超时ms（不宜过长），默认200
     */
    int connectionRequestTimeout = 200;
    /**
     * 缓冲请求数据，默认false，通过POST或者PUT大量发送数据时，建议不要修改，以免耗尽内存（注意：Spring boot 1.5.x及以下需要设置为true）
     */
    boolean enableBufferRequestBody = false;
    /**
     * 连接保活时长ms，默认5000
     */
    int keepAlive = 5000;
    /**
     * 允许重试，默认为true
     */
    boolean enableRequestSentRetry = true;
    /**
     * 重试次数，默认3次
     */
    int retryCount = 3;
}
