package com.github.yizzuide.milkomeda.atom;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableAtom
 *
 * @author yizzuide
 * @since 3.3.0
 * Create at 2020/04/30 15:13
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(AtomConfig.class)
public @interface EnableAtom {
}
