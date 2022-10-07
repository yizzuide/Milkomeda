package com.github.yizzuide.milkomeda.demo.echo.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.Name;

/**
 * ThirdKey
 *
 * @author yizzuide
 * <br />
 * Create at 2022/02/12 23:30
 */
@ConfigurationProperties(prefix = "third")
public class ThirdKey {
    @Setter @Getter
    private String parPubKey;
    @Setter @Getter
    private String priKey;

    public ThirdKey() {
        parPubKey = "";
        priKey = "";
    }

    // Springboot 2.2.0 推出的构造器绑定
    // 单个参数构造器时，直接放在类上
    // 在提供多个构造器情况下，必需放在指定的构造器上
    // Springboot 2.4 提供@Name自定义参数名，@DurationUnit, @DataSizeUnit, and @PeriodUnit can annotate a constructor parameter using @ConstructorBinding
    @ConstructorBinding
    public ThirdKey(@Name("parPubKey") String parPubKey, String priKey) {
        this.parPubKey = parPubKey;
        this.priKey = priKey;
    }
}
