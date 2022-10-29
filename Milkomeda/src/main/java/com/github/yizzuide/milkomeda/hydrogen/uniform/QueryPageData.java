package com.github.yizzuide.milkomeda.hydrogen.uniform;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Request query page type data.
 *
 * @since 3.14.0
 * @author yizzuide
 * <br>
 * Create at 2022/10/29 19:10
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QueryPageData<T> extends QueryData<T> {
    /**
     * 查询当前页
     */
    private Integer pageStart;
    /**
     * 每页记录数
     */
    private Integer pageSize;
}
