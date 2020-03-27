package com.github.yizzuide.milkomeda.hydrogen.validator;

import org.apache.commons.lang3.time.DateUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * DateConstraintValidator
 *
 * @author yizzuide
 * @since 2.8.0
 * Create at 2020/03/26 20:55
 */
public class DateConstraintValidator implements ConstraintValidator<DateConstraint, String> {
    /**
     * 日期格式
     */
    private String dateFormat;

    @Override
    public void initialize(DateConstraint constraintAnnotation) {
        this.dateFormat = constraintAnnotation.dateFormat();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        // 如果为空就不验证
        if (null == value || "".equals(value)) return true;
        try {
            DateUtils.parseDate(value, dateFormat);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
