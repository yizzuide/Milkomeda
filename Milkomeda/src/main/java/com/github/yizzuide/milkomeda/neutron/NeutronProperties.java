package com.github.yizzuide.milkomeda.neutron;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * NeutronProperties
 *
 * @author yizzuide
 * @version 1.18.0
 * Create at 2019/12/09 22:54
 */
@Data
@ConfigurationProperties("milkomeda.neutron")
public class NeutronProperties {
    /** 数据库持久化 */
    private boolean serializable = true;

}
