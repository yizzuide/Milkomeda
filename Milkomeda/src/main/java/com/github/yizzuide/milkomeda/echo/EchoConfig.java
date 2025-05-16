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
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * HttpClientConfig
 *
 * @author yizzuide
 * @since 1.13.0
 * @version 3.20.0
 * <br>
 * Create at 2019/09/21 16:24
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(EchoProperties.class)
public class EchoConfig {

    @Autowired
    private EchoProperties echoProperties;

    @Bean("echoRestTemplate")
    public RestTemplate simpleRestTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder.build();
        // Spring Boot 3.0：Spring Framework 6.0 中已删除对 Apache HttpClient 的支持，现在由 org.apache.httpcomponents.client5:httpclient5 取代
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

        // 配置HTTPS支持
        Registry<ConnectionSocketFactory> socketFactoryRegistry = null;
        SSLContext sslContext = null;
        try {
            // 1. setup a Trust Strategy that allows all certificates.
            TrustStrategy acceptingTrustStrategy = (chain, authType) -> true;
            sslContext = org.apache.http.ssl.SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();

            // 2. don't check Host names, either.
            // -- use SSLConnectionSocketFactory.getDefaultHostnameVerifier(), if you don't want to weaken
            HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;

            // 3. here's the special part:
            //      -- need to create an SSL Socket Factory, to use our weakened "trust strategy";
            //      -- and create a Registry, to register it.
            //
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
            socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslSocketFactory)
                    .build();
        } catch (Exception e) {
            log.error("Echo create httpClient SSL error", e);
        }

        // 4. now, we create connection-manager using our Registry.
        //      -- allows multi-threaded use
        PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager(Objects.requireNonNull(socketFactoryRegistry));;
        poolingConnectionManager.setMaxTotal(echoProperties.getPoolMaxSize()); // 连接池最大连接数
        poolingConnectionManager.setDefaultMaxPerRoute(echoProperties.getDefaultMaxPerRoute()); // 每个路由的并发

        // 自定义连接管理
        return HttpClients.custom().setConnectionManager(poolingConnectionManager).setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                long keepAlive = super.getKeepAliveDuration(response, context);
                if (keepAlive == -1) {
                    keepAlive = echoProperties.getKeepAlive().toMillis();
                }
                return keepAlive;
            }
        }).setRetryHandler(new DefaultHttpRequestRetryHandler(echoProperties.getRetryCount(), echoProperties.isEnableRequestSentRetry()))
                .setSSLContext(sslContext)
                .setDefaultHeaders(headers).build();
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setHttpClient(closeableHttpClient());
        //设置客户端和服务端建立连接的超时时间
        clientHttpRequestFactory.setConnectTimeout((int) echoProperties.getConnectTimeout().toMillis());
        //设置客户端从服务端读取数据的超时时间
        clientHttpRequestFactory.setReadTimeout((int) echoProperties.getConnectTimeout().toMillis());
        //设置从连接池获取连接的超时时间，不宜过长
        clientHttpRequestFactory.setConnectionRequestTimeout((int) echoProperties.getConnectionRequestTimeout().toMillis());
        //缓冲请求数据，默认为true。通过POST或者PUT大量发送数据时，建议将此更改为false，以免耗尽内存（注意：Spring boot 1.5.x下需要注释掉这行）
        clientHttpRequestFactory.setBufferRequestBody(false);
        return clientHttpRequestFactory;
    }
}