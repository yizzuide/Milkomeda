package com.github.yizzuide.milkomeda.demo.echo.config;

import com.github.yizzuide.milkomeda.demo.echo.props.ThirdKey;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Config
 *
 * @author yizzuide
 * <br>
 * Create at 2022/02/12 23:40
 */
@Configuration
@EnableConfigurationProperties(ThirdKey.class)
public class Config {

    @Value("${openApi.appId}")
    private String appId;

    @PostConstruct
    public void config() {
        System.out.println(appId);
    }



    // 对象Setter绑定方式
    /*@ConfigurationProperties(prefix = "third")
    @Bean
    public ThirdKey thirdKey() {
        return new ThirdKey();
    }*/

}
