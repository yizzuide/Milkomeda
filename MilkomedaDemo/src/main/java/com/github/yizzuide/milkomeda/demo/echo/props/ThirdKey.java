package com.github.yizzuide.milkomeda.demo.echo.props;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * ThirdKey
 *
 * @author yizzuide
 * Create at 2022/02/12 23:30
 */
@ConfigurationProperties(prefix = "third")
public class ThirdKey {
    @Getter
    private final String parPubKey;
    @Getter
    private final String priKey;

    public ThirdKey() {
        parPubKey = "";
        priKey = "";
    }

    // Springboot 2.2.0 推出的构造器绑定
    // 单个参数构造器时，直接放在类上
    // 在提供多个构造器情况下，必需放在指定的构造器上
    @ConstructorBinding
    public ThirdKey(String parPubKey, String priKey) {
        this.parPubKey = parPubKey;
        this.priKey = priKey;
    }
}
