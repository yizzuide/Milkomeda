package com.github.yizzuide.milkomeda.hydrogen.uniform;

import lombok.Data;

import java.util.Date;

/**
 * Request query data.
 *
 * @since 3.14.0
 * @author yizzuide
 * <br>
 * Create at 2022/10/29 19:09
 */
@Data
public class QueryData<T> {
    /**
     * 模型实体
     */
    private T entity;
    /**
     * 开始时间
     */
    private Date startDate;
    /**
     * 结束时间
     */
    private Date endDate;
}
