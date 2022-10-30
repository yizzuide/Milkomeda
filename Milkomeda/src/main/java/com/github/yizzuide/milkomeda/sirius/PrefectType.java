package com.github.yizzuide.milkomeda.sirius;

import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformQueryPageData;

/**
 * Prefect type using for {@link QueryMatcher}
 *
 * @since 3.14.0
 * @author yizzuide
 * <br>
 * Create at 2022/10/30 16:59
 */
public enum PrefectType {
    /**
     * equals field.
     */
    EQ,
    /**
     * sql like field.
     */
    LIKE,
    /**
     * sql order by.
     */
    OrderByPre,
    /**
     * page result list order by.
     */
    OrderByPost,
    /**
     * using for `startDate` and `endDate` of {@link UniformQueryPageData} query.
     */
    PageDate
}
