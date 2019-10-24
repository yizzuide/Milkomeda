package com.github.yizzuide.milkomeda.echo;


import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.conn.HttpClientConnectionManager;
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
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * HttpClientConfig
 *
 * @author yizzuide
 * @since 1.13.0
 * @version 1.13.4
 * Create at 2019/09/21 16:24
 */
@Slf4j
@Configuration
@EnableScheduling
@EnableConfigurationProperties(EchoProperties.class)
public class EchoConfig {

    @Autowired
    private EchoProperties echoProperties;

    // 用于Spring Cloud体系
   /* @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }*/

    @Bean("echoRestTemplate")
    public RestTemplate simpleRestTemplate(RestTemplateBuilder builder) {
        return getRestTemplate(builder);
    }

    private RestTemplate getRestTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder.build();
        restTemplate.setRequestFactory(clientHttpRequestFactory());
        // 使用 utf-8 编码集的 Converter 替换默认的 Converter（默认的 string Converter 的编码集为"ISO-8859-1"）
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        messageConverters.removeIf(converter -> converter instanceof StringHttpMessageConverter);
        messageConverters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
        // 自定义错误处理
        restTemplate.setErrorHandler(new EchoResponseErrorHandler());
        return restTemplate;
    }



    @Bean
    public HttpClientConnectionManager poolingConnectionManager() {
        PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager();
        poolingConnectionManager.setMaxTotal(echoProperties.getPoolMaxSize()); // 连接池最大连接数
        poolingConnectionManager.setDefaultMaxPerRoute(echoProperties.getDefaultMaxPerRoute()); // 每个路由的并发
        return poolingConnectionManager;
    }

    @Bean
    public CloseableHttpClient closeableHttpClient() {
        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.04"));
        headers.add(new BasicHeader("Accept-Encoding", "gzip, deflate"));
        headers.add(new BasicHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3"));
        headers.add(new BasicHeader("Connection", "Keep-Alive"));

        // 配置HTTPS支持
        SSLConnectionSocketFactory csf=null;
        try {
            TrustStrategy acceptingTrustStrategy = (chain, authType) -> true;
            SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();
            csf = new SSLConnectionSocketFactory(sslContext);
        } catch (Exception e) {
            log.error("httpClient SSL error", e);
        }
        // 自定义连接管理
        return HttpClients.custom().setConnectionManager(poolingConnectionManager()).setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                long keepAlive = super.getKeepAliveDuration(response, context);
                if (keepAlive == -1) {
                    keepAlive = echoProperties.getKeepAlive();
                }
                return keepAlive;
            }
        }).setRetryHandler(new DefaultHttpRequestRetryHandler(echoProperties.getRetryCount(), echoProperties.isEnableRequestSentRetry())).setSSLSocketFactory(csf)
                .setDefaultHeaders(headers).build();
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setHttpClient(closeableHttpClient());
        //设置客户端和服务端建立连接的超时时间
        clientHttpRequestFactory.setConnectTimeout(echoProperties.getConnectTimeout());
        //设置客户端从服务端读取数据的超时时间
        clientHttpRequestFactory.setReadTimeout(echoProperties.getReadTimeout());
        //设置从连接池获取连接的超时时间，不宜过长
        clientHttpRequestFactory.setConnectionRequestTimeout(echoProperties.getConnectionRequestTimeout());
        //缓冲请求数据，默认为true。通过POST或者PUT大量发送数据时，建议将此更改为false，以免耗尽内存（注意：Spring boot 1.5.x下需要注释掉这行）
        clientHttpRequestFactory.setBufferRequestBody(echoProperties.isEnableBufferRequestBody());
        return clientHttpRequestFactory;
    }
}