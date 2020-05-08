package com.github.yizzuide.milkomeda.sundial;

import java.lang.annotation.*;

/**
 * @date: 2020/5/7
 * @author: jsq
 * @email: 786063250@qq.com
 * @describe: 数据源
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SundialDynamicDataSource {

    DataSourceType value() default DataSourceType.MASTER;
}
