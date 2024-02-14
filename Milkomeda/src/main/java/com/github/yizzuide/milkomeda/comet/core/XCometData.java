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

package com.github.yizzuide.milkomeda.comet.core;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * Comet log for business transaction.
 *
 * @author yizzuide
 * @since 1.12.0
 * @version 4.0.0
 * <br>
 * Create at 2019/09/21 10:55
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class XCometData extends CometData {
    @Serial
    private static final long serialVersionUID = 6781190633733977589L;

    /**
     * CometX DDD event action.
     */
    public static final String ACTION = "COMET_X_ACTION";

    /**
     * Subject id related to the execute method.
     */
    private Serializable subjectId = 0;

    /**
     * Who execute the method.
     */
    private Serializable operator = 0;

    /**
     * Record info after method execute success.
     */
    private String successInfo;

    /**
     * Other details useful info
     */
    private String detailInfo;

    /**
     * The method return value.
     */
    private transient Object result;

    /**
     * The method context variables.
     */
    private transient Map<String, Object> variables;

    public Long getSubjectIdLong() {
        if (this.subjectId instanceof Long) {
            return (Long) this.subjectId;
        }
        return Long.valueOf(this.subjectId.toString());
    }

    public Long getOperatorLong() {
        if (this.operator instanceof Long) {
            return (Long) this.operator;
        }
        return Long.valueOf(this.operator.toString());
    }
}
