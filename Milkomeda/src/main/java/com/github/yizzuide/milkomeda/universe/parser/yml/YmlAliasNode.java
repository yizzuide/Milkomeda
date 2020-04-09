package com.github.yizzuide.milkomeda.universe.parser.yml;

import lombok.Data;

/**
 * YmlAliasNode
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/03/29 19:29
 */
@Data
public class YmlAliasNode {
    /**
     * 别名键
     */
    private String key;
    /**
     * 别名值
     */
    private Object value;
}
