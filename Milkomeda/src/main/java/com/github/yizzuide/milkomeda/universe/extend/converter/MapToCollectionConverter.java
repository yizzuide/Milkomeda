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

package com.github.yizzuide.milkomeda.universe.extend.converter;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.CollectionFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.util.ClassUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Converts a Map to Collection.
 * <p>
 * For similar implementation, refer to MapToMapConverter or CollectionToCollectionConverter
 * </p>
 * @author yizzuide
 * @since 3.13.0
 * @see org.springframework.core.convert.support.DefaultConversionService
 * @see org.springframework.context.support.ConversionServiceFactoryBean
 * <br>
 * Create at 2022/08/30 19:46
 */
public class MapToCollectionConverter implements ConditionalGenericConverter {

    private final ConversionService conversionService;


    public MapToCollectionConverter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public boolean matches(@NotNull TypeDescriptor sourceType, @NotNull TypeDescriptor targetType) {
        TypeDescriptor sourceElementType = sourceType.getMapValueTypeDescriptor();
        TypeDescriptor targetElementType = targetType.getElementTypeDescriptor();
        if (sourceElementType == null || targetElementType == null) {
            return true;
        }
        return conversionService.canConvert(sourceElementType, targetElementType) ||
                ClassUtils.isAssignable(sourceElementType.getType(), targetElementType.getType());
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(Map.class, Collection.class));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object convert(Object source, @NotNull TypeDescriptor sourceType, @NotNull TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        Map<Object, Object> sourceMap = (Map<Object, Object>) source;
        // Only add values
        Collection<Object> sourceCollection = sourceMap.values();
        TypeDescriptor elementDesc = targetType.getElementTypeDescriptor();
        Collection<Object> target = CollectionFactory.createCollection(targetType.getType(),
                (elementDesc != null ? elementDesc.getType() : null), sourceCollection.size());
        if (elementDesc == null) {
            target.addAll(sourceCollection);
            return target;
        }

        for (Object sourceElement : sourceCollection) {
            // ConversionService.convert support recursive call of compound type
            Object targetElement = this.conversionService.convert(sourceElement,
                    sourceType.elementTypeDescriptor(sourceElement), elementDesc);
            target.add(targetElement);
        }
        return target;
    }
}
