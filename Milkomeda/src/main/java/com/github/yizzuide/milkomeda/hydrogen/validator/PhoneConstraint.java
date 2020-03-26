package com.github.yizzuide.milkomeda.hydrogen.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * PhoneConstraint
 *
 * @author yizzuide
 * @since 2.8.0
 * Create at 2020/03/26 20:20
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneConstraintValidator.class)
public @interface PhoneConstraint {
    /**
     * 定义默认验证失败的消息
     * @return String
     */
    String message() default "phone is invalid";

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
}
