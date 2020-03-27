package com.github.yizzuide.milkomeda.hydrogen.validator;

import com.github.yizzuide.milkomeda.hydrogen.core.HydrogenHolder;

import javax.validation.ConstraintViolation;
import java.util.Set;

/**
 * ValidatorExaminer
 *
 * @author yizzuide
 * @since 2.8.0
 * Create at 2020/03/26 21:12
 */
public class ValidatorExaminer {
    /**
     * 参数校验
     * @param obj       参数对象
     * @param groups    校验分组
     * @param <T>       参数对象类型
     * @return 错误信息
     */
    public static <T> String valid(T obj, Class<?>... groups) {
        Set<ConstraintViolation<T>> violationSet = HydrogenHolder.getValidator().validate(obj, groups);
        if (violationSet.size() > 0) {
            ConstraintViolation<T> model = violationSet.iterator().next();
            return model.getMessage();
        }
        return null;
    }
}
