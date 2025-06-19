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

package com.github.yizzuide.milkomeda.echo;


import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * HttpClientConfig
 *
 * @author yizzuide
 * @since 1.13.0
 * @version 4.0.0
 * <br>
 * Create at 2019/09/21 16:24
 */
// Spring Boot 3.2: Spring Boot 3.2 includes support for the new RestClient interface which has been introduced in Spring Framework 6.1.
//  This interface provides a functional style blocking HTTP API with a similar to design to WebClient.
//  Existing and new application might want to consider using RestClient as an alternative to RestTemplate.
@Slf4j
@Configuration
@EnableConfigurationProperties(EchoProperties.class)
public class EchoConfig {

    @Autowired
    private EchoProperties echoProperties;

    // Spring Boot 3.0: RestTemplate, or rather the HttpComponentsClientHttpRequestFactory, now requires Apache HttpClient 5.
    //  If you are noticing issues with HTTP client behavior, it could be that RestTemplate is falling back to the JDK client.
    //  `org.apache.httpcomponents:httpclient` can be brought transitively by other dependencies, so your application might rely on this dependency without declaring it.
    // Since RestTemplate instances often need to be customized before being used, Spring Boot does not provide any single Auto-configured RestTemplate bean.
    //  The Auto-configured RestTemplateBuilder ensures that sensible HttpMessageConverters and an appropriate ClientHttpRequestFactory are applied to RestTemplate instances.
    @Bean("echoRestTemplate")
    public RestTemplate simpleRestTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder/*.setSslBundle()*/.build();
        // Spring Boot 3.1：Support for Apache HttpClient 4 with RestTemplate was removed in Spring Framework 6,in favor of Apache HttpClient 5.
        restTemplate.setRequestFactory(clientHttpRequestFactory());
        // 自定义错误处理
        restTemplate.setErrorHandler(new EchoResponseErrorHandler());
        return restTemplate;
    }

    @Bean
    public CloseableHttpClient closeableHttpClient() {
        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.04"));
        headers.add(new BasicHeader("Accept-Encoding", "gzip, deflate"));
        headers.add(new BasicHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3"));
        headers.add(new BasicHeader("Connection", "Keep-Alive"));

        final RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(echoProperties.getConnectionRequestTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .setResponseTimeout(echoProperties.getResponseTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .build();
        final BasicCookieStore defaultCookieStore = new BasicCookieStore();
        SSLContext sslContext = null;
        try {
            sslContext  = SSLContexts.custom()
                    .loadTrustMaterial(null, new TrustAllStrategy()).build();
        } catch (Exception e) {
            log.error("Echo create httpClient SSL error", e);
        }

        final SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                .setSslContext(sslContext).build();
        final HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .setMaxConnTotal(echoProperties.getPoolMaxSize()) // 连接池最大连接数
                .setMaxConnPerRoute(echoProperties.getDefaultMaxPerRoute()) // 每个路由的并发
                .build();

        // 自定义连接管理
        return HttpClients.custom()
                .setDefaultCookieStore(defaultCookieStore)
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(defaultRequestConfig)
                .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy() {
                    @Override
                    public TimeValue getKeepAliveDuration(HttpResponse response, HttpContext context) {
                        TimeValue keepAlive = super.getKeepAliveDuration(response, context);
                        if (keepAlive.getDuration() == -1) {
                            return TimeValue.of(echoProperties.getKeepAlive());
                        }
                        return super.getKeepAliveDuration(response, context);
                    }
                })
                .setRetryStrategy(new DefaultHttpRequestRetryStrategy(echoProperties.isEnableRequestSentRetry() ? echoProperties.getRetryCount() : 0, TimeValue.of(Duration.ZERO)))
                .setDefaultHeaders(headers)
                .build();
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        // Spring Boot 3.0: ClientHttpRequestFactorySupplier should be replaced with ClientHttpRequestFactories
        /*ClientHttpRequestFactory defaultClientHttpRequestFactory = ClientHttpRequestFactories.get(
                HttpComponentsClientHttpRequestFactory.class,
                ClientHttpRequestFactorySettings.DEFAULTS.withConnectTimeout(echoProperties.getConnectTimeout())
        );*/
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setHttpClient(closeableHttpClient());
        // 设置客户端和服务端建立连接的超时时间
        clientHttpRequestFactory.setConnectTimeout((int) echoProperties.getConnectTimeout().toMillis());
        return clientHttpRequestFactory;
    }
}