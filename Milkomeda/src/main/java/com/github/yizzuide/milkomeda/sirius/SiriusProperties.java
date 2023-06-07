/*
 * Copyright (c) 2022 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.sirius;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.convert.converter.GenericConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Sirius module properties
 *
 * @since 3.14.0
 * @version 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2022/10/30 17:52
 */
@Data
@ConfigurationProperties(prefix = SiriusProperties.PREFIX)
public class SiriusProperties {
    public static final String PREFIX = "milkomeda.sirius";

    /**
     * Database type for mybatis-plus pagination interceptor.
     */
    private DbType dbType = DbType.MYSQL;

    /**
     * Automatically identify entity fill attributes without adding `@TableField(fill = xxx)` annotations
     * @since 3.15.0
     */
    private boolean autoAddFill = true;

    /**
     * Auto value interpolation.
     */
    private List<AutoInterpolate> autoInterpolates = new ArrayList<>();

    @Data
    static class AutoInterpolate {
        /**
         * What common field need interpolate to data table.
         */
        private List<String> fields;

        /**
         * Property source value, can be with Spring EL using `el(condition, type)`.
         */
        private String psValue;

        /**
         * convert property source value.
         */
        private Class<GenericConverter> converterClazz;

        /**
         * Field fill type.
         */
        private FieldFill fieldFill = FieldFill.INSERT;
    }
}
