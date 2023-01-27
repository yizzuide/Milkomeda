package com.github.yizzuide.milkomeda.demo.orbit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RecordLog
 *
 * @author yizzuide
 * <br>
 * Create at 2023/01/28 01:33
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RecordLog {
}
