package com.github.yizzuide.milkomeda.rock;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("milkomeda.rock")
public class RockProperties {

    private String loginUrl;

    private String successUrl;

    private String unauthorizedUrl;
}
