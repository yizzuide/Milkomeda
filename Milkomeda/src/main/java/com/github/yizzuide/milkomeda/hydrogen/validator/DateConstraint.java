package com.github.yizzuide.milkomeda.hydrogen.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * DateConstraint
 *
 * @author yizzuide
 * @since 2.8.0
 * Create at 2020/03/26 20:52
 */
@Documented
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateConstraintValidator.class)
public @interface DateConstraint {
    /**
     * 定义默认验证失败的消息
     * @return String
     */
    String message() default "{hydrogen.validation.constraints.DateConstraint.message}";

    /**
     * 所属分组，默认在 Default 分组
     * @return Class列表
     */
    Class<?>[] groups() default {};

    /**
     * 主要是针对bean的
     * @return Class
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * 日期格式
     * @return String
     */
    String dateFormat() default "yyyy-MM-dd HH:mm:ss";
}
