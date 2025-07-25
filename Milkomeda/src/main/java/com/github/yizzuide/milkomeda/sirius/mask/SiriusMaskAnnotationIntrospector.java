/*
 * Copyright (c) 2025 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.sirius.mask;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.github.yizzuide.milkomeda.util.TypeUtil;

import java.io.Serial;
import java.util.List;

/**
 * Sensitive field masking annotation introspect with {@link ObjectMapper}.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/07/24 17:17
 */
public class SiriusMaskAnnotationIntrospector extends NopAnnotationIntrospector {

    @Serial
    private static final long serialVersionUID = 7753413878742607521L;

    private final SiriusMaskProperties maskProperties;

    private final List<MaskFilter> maskFilters;

    public SiriusMaskAnnotationIntrospector(SiriusMaskProperties maskProperties, List<MaskFilter> maskFilters) {
        this.maskProperties = maskProperties;
        this.maskFilters = maskFilters;
    }

    @Override
    public Object findSerializer(Annotated am) {
        boolean useMask = false;
        if (TypeUtil.type2JavaType(String.class).equals(am.getType()) && maskProperties.getMaskFields() != null) {
            if (maskProperties.getMaskFields().stream().anyMatch(name -> name.equals(am.getName()))) {
                if (maskFilters != null) {
                    for (MaskFilter maskFilter : maskFilters) {
                        if (!maskFilter.filter(am)) {
                            useMask = true;
                            break;
                        }
                    }
                }
            }
        }
        if (!useMask) {
            MaskField annotation = am.getAnnotation(MaskField.class);
            if (annotation != null) {
                if (maskFilters != null) {
                    for (MaskFilter maskFilter : maskFilters) {
                        if (!maskFilter.filter(am)) {
                            useMask = true;
                            break;
                        }
                    }
                }
            }
        }

        if (useMask) {
            return new SiriusMaskingSerializer(this.maskProperties.getMaskChar());
        }
        return null;
    }
}