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

package com.github.yizzuide.milkomeda.universe.parser.placeholder;

import com.github.yizzuide.milkomeda.util.ReflectUtil;
import com.github.yizzuide.milkomeda.util.Strings;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.PropertyPlaceholderHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 占位符提取器
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.13.0
 * Creat at 2020/03/28 11:08
 */
@Slf4j
public class PlaceholderExtractor extends PropertyPlaceholderHelper {
    /**
     * 默认前缀占位符
     */
    public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";

    /**
     * 默认后缀占位符
     */
    public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

    /**
     * Default separator used in placeholder
     */
    public static final String DEFAULT_SEPARATOR = ":";

    /**
     * 占位符前缀
     */
    private final String placeholderPrefix;

    /**
     * 占位符后缀
     */
    private final String placeholderSuffix;

    /**
     * 默认单例解析器
     */
    private static final PlaceholderExtractor defaultExtractor = new PlaceholderExtractor();


    private PlaceholderExtractor() {
        this(DEFAULT_PLACEHOLDER_PREFIX, DEFAULT_PLACEHOLDER_SUFFIX);
    }

    private PlaceholderExtractor(String placeholderPrefix, String placeholderSuffix) {
        super(placeholderPrefix, placeholderSuffix, DEFAULT_SEPARATOR, true);
        this.placeholderPrefix = placeholderPrefix;
        this.placeholderSuffix = placeholderSuffix;
    }

    /**
     * Create PlaceholderResolver with placeholderPrefix, the default placeholderSuffix is "}"
     * @param placeholderPrefix placeholder prefix
     * @return PlaceholderResolver
     * @since 3.13.0
     */
    public static PlaceholderExtractor create(String placeholderPrefix) {
        return new PlaceholderExtractor(placeholderPrefix, DEFAULT_PLACEHOLDER_SUFFIX);
    }

    /**
     * Create PlaceholderResolver with placeholderPrefix and placeholderSuffix
     * @param placeholderPrefix placeholder prefix
     * @param placeholderSuffix placeholder suffix
     * @return PlaceholderResolver
     */
    public static PlaceholderExtractor create(String placeholderPrefix, String placeholderSuffix) {
        return new PlaceholderExtractor(placeholderPrefix, placeholderSuffix);
    }

    /**
     * 获取所有占位符
     *
     * @param value   模板字符串
     * @return  占位符列表
     */
    public List<String> getPlaceHolders(String value) {
        int start = value.indexOf(this.placeholderPrefix);
        if (start == -1) {
            return null;
        }
        List<String> keys = new ArrayList<>();
        while (start != -1) {
            int end = value.indexOf(this.placeholderSuffix, start + 1);
            String subContent = value.substring(start + 1, end);
            // 忽略包含有占位符的内容
            if (subContent.contains(this.placeholderPrefix)) {
                start = value.indexOf(this.placeholderPrefix, start + 1);
                continue;
            }
            keys.add(subContent);
            start = value.indexOf(this.placeholderPrefix, end + 1);
        }
        return keys;
    }

    /**
     * 解析带有指定占位符的模板字符串
     *
     * @param value 要解析的带有占位符的模板字符串
     * @param values   按照模板占位符索引位置设置对应的值
     * @return  解析的字符串
     */
    public String replacePlaceholders(String value, String... values) {
        int start = value.indexOf(this.placeholderPrefix);
        if (start == -1) {
            return value;
        }
        // 值索引
        int valueIndex = 0;
        StringBuilder result = new StringBuilder(value);
        while (start != -1) {
            int end = result.indexOf(this.placeholderSuffix);
            String replaceContent = values[valueIndex++];
            result.replace(start, end + this.placeholderSuffix.length(), replaceContent);
            start = result.indexOf(this.placeholderPrefix, start + replaceContent.length());
        }
        return result.toString();
    }

    /**
     * 解析带有指定占位符的模板字符串
     *
     * @param value 要解析的带有占位符的模板字符串
     * @param values    按照模板占位符索引位置设置对应的值
     * @return  解析的字符串
     */
    public String replacePlaceholders(String value, Object[] values) {
        return replacePlaceholders(value, Stream.of(values).map(String::valueOf).toArray(String[]::new));
    }

    @NotNull
    @Override
    public String replacePlaceholders(@NotNull String value, @NotNull PlaceholderResolver placeholderResolver) {
        return super.replacePlaceholders(value, placeholderName -> updateDefaultValue(placeholderName, placeholderResolver.resolvePlaceholder(placeholderName)));
    }

    @NotNull
    public String replacePlaceholders(@NotNull String value, @NotNull PlaceholderResolver placeholderResolver, boolean recursive) {
        if (recursive) {
            return replacePlaceholders(value, placeholderResolver);
        }
        return findAndReplace(value, placeholderResolver);
    }

    /**
     * 替换模板中占位符内容，占位符的内容即为map，key为占位符中的内容
     *
     * @param value  模板内容
     * @param valueMap 值映射
     * @return   替换完成后的字符串
     */
    public String replacePlaceholders(String value, final Map<String, Object> valueMap) {
        return replacePlaceholders(value, placeholderName -> Strings.toNullableString(valueMap.get(placeholderName)) , false);
    }

    /**
     * 根据对象中字段路径(即类似js访问对象属性值)替换模板中的占位符
     *
     * @param value  要解析的内容
     * @param obj    填充解析内容的对象(如果是基本类型，则所有占位符替换为相同的值)
     * @return  替换完成后的字符串
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String replacePlaceholders(String value, final Object obj) {
        if (obj instanceof Map) {
            return replacePlaceholders(value, (Map) obj);
        }
        return replacePlaceholders(value, placeholderName -> ReflectUtil.invokeFieldPath(obj, placeholderName), false);
    }

    /**
     * Find and replace value with placeholder, not support recursive
     * @param value template string
     * @param placeholderResolver   functional placeholder resolver
     * @return  replaced value
     * @since 3.13.0
     */
    private String findAndReplace(String value, PlaceholderResolver placeholderResolver) {
        int start = value.indexOf(this.placeholderPrefix);
        if (start == -1) {
            return value;
        }
        StringBuilder result = new StringBuilder(value);
        while (start != -1) {
            int end = result.indexOf(this.placeholderSuffix, start);
            // 获取占位符属性值，如${id}, 即获取id
            String placeholder = result.substring(start + this.placeholderPrefix.length(), end);
            if (placeholder.contains(this.placeholderPrefix)) {
                start = value.indexOf(this.placeholderPrefix, start + 1);
                continue;
            }
            // 替换整个占位符内容，即将${id}值替换为替换规则回调中的内容
            String replaceContent = placeholder.trim().isEmpty() ? "" : placeholderResolver.resolvePlaceholder(placeholder);
            // Find default value for look separator
            if (replaceContent == null && placeholder.contains(DEFAULT_SEPARATOR)) {
                int separatorIndex = placeholder.indexOf(DEFAULT_SEPARATOR);
                if (separatorIndex != -1) {
                    String actualPlaceholder = placeholder.substring(0, separatorIndex);
                    String defaultValue = placeholder.substring(separatorIndex + DEFAULT_SEPARATOR.length());
                    replaceContent = placeholderResolver.resolvePlaceholder(actualPlaceholder);
                    if (replaceContent == null) {
                        replaceContent = defaultValue;
                    }
                }
            }

            // If replace content is still null, set "null"
            replaceContent = updateDefaultValue(placeholder, replaceContent);

            result.replace(start, end + this.placeholderSuffix.length(), replaceContent);
            start = result.indexOf(this.placeholderPrefix, start + replaceContent.length());
        }
        return result.toString();
    }

    /**
     * Update replace value with default value "null"
     * @param placeholderName  current placeholder name
     * @param replaceValue  current replace value
     * @return Updated replace value
     * @since 3.13.0
     */
    private String updateDefaultValue(String placeholderName, String replaceValue) {
        if (replaceValue != null || placeholderName.contains(DEFAULT_SEPARATOR)) {
            return replaceValue;
        }
        return "null";
    }
}