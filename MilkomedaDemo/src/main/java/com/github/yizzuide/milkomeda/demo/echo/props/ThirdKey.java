package com.github.yizzuide.milkomeda.demo.echo.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.Name;

/**
 * ThirdKey
 *
 * @author yizzuide
 * <br>
 * Create at 2022/02/12 23:30
 */
@Data
@ConfigurationProperties(prefix = "third")
public class ThirdKey {
    private String parPubKey;
    private String priKey;

    public ThirdKey() {
        parPubKey = "";
        priKey = "";
    }

    // Spring Boot 2.2: 推出的构造器绑定，单个参数构造器时直接放在类上；在提供多个构造器情况下，必需放在指定的构造器上
    // Spring Boot 2.4: 提供@Name自定义参数名，@DurationUnit, @DataSizeUnit, and @PeriodUnit can annotate a constructor parameter using @ConstructorBinding
    // Spring Boot 2.6: If you are using @ConfigurationProperties with a Java 16 record and the record has a single constructor, it no longer needs to be annotated with @ConstructorBinding.
    // Spring Boot 3.0: 在单个构造器时，@ConstructorBinding可以省略；但多个构造器下指定构造器时，@ConstructorBinding必须指定
    @ConstructorBinding
    public ThirdKey(@Name("parPubKey") String parPubKey, String priKey) {
        this.parPubKey = parPubKey;
        this.priKey = priKey;
    }
}
