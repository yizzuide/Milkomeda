package com.github.yizzuide.milkomeda.hydrogen.validator;

import com.github.yizzuide.milkomeda.hydrogen.core.HydrogenHolder;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * PhoneConstraintValidator
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/03/26 20:20
 */
public class PhoneConstraintValidator implements ConstraintValidator<PhoneConstraint, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 如果为空就不验证
        if (null == value || "".equals(value)) return true;
        return Pattern.matches(HydrogenHolder.getProps().getValidator().getRegexPhone(), value);
    }
}
