/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.hydrogen.validator;

import com.github.yizzuide.milkomeda.hydrogen.core.HydrogenHolder;

import javax.validation.ConstraintViolation;
import java.util.Set;

/**
 * ValidatorExaminer
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/03/26 21:12
 */
public class ValidatorExaminer {
    /**
     * 参数校验
     * @param obj       参数对象
     * @param groups    校验分组
     * @param <T>       参数对象类型
     * @return 错误信息，如果返回null，则验证成功
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
