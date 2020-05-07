package com.github.yizzuide.milkomeda.wormhole;

<<<<<<< HEAD

/**
 * EnableWormhole
 * @author jsq
 * @since 1.16.0
 * Create at 2019/11/23 00:18
 */
public interface EnableWormhole {
=======
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableWormhole
 *
 * @author yizzuide
 * @since 3.3.0
 * Create at 2020/05/05 13:57
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(WormholeConfig.class)
public @interface EnableWormhole {
>>>>>>> 5363ee339f0dcb5d921d49d83dd3f6064f4ac8d2
}
