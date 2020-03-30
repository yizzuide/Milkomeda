package com.github.yizzuide.milkomeda.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * 占位符解析器
 *
 * @author yizzuide
 * @since 3.0.0
 * Creat at 2020/03/28 11:08
 */
@Slf4j
public class PlaceholderResolver {
    /**
     * 默认前缀占位符
     */
    public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";

    /**
     * 默认后缀占位符
     */
    public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

    /**
     * 默认单例解析器
     */
    private static PlaceholderResolver defaultResolver = new PlaceholderResolver();

    /**
     * 占位符前缀
     */
    private String placeholderPrefix = DEFAULT_PLACEHOLDER_PREFIX;

    /**
     * 占位符后缀
     */
    private String placeholderSuffix = DEFAULT_PLACEHOLDER_SUFFIX;


    private PlaceholderResolver(){}

    private PlaceholderResolver(String placeholderPrefix, String placeholderSuffix) {
        this.placeholderPrefix = placeholderPrefix;
        this.placeholderSuffix = placeholderSuffix;
    }

    /**
     * 获取默认的占位符解析器，即占位符前缀为"${", 后缀为"}"
     * @return  PlaceholderResolver
     */
    public static PlaceholderResolver getDefaultResolver() {
        return defaultResolver;
    }

    public static PlaceholderResolver getResolver(String placeholderPrefix, String placeholderSuffix) {
        return new PlaceholderResolver(placeholderPrefix, placeholderSuffix);
    }

    /**
     * 获取所有占位符
     * @param content   模板字符串
     * @return  占位符列表
     */
    public List<String> getPlaceHolders(String content) {
        int start = content.indexOf(this.placeholderPrefix);
        if (start == -1) {
            return null;
        }
        List<String> keys = new ArrayList<>();
        while (start != -1) {
            int end = content.indexOf(this.placeholderSuffix, start + 1);
            keys.add(content.substring(start + 1, end));
            start = content.indexOf(this.placeholderPrefix, end + 1);
        }
        return keys;
    }

    /**
     * 解析带有指定占位符的模板字符串 <br/>
     * <pre>
     *  content = "category:${}:product:${}"
     *  values = {"1", "2"}
     *  output: "category:1:product:2"
     * </pre>
     *
     * @param content 要解析的带有占位符的模板字符串
     * @param values   按照模板占位符索引位置设置对应的值
     * @return  解析的字符串
     */
    public String resolve(String content, String... values) {
        int start = content.indexOf(this.placeholderPrefix);
        if (start == -1) {
            return content;
        }
        // 值索引
        int valueIndex = 0;
        StringBuilder result = new StringBuilder(content);
        while (start != -1) {
            int end = result.indexOf(this.placeholderSuffix);
            String replaceContent = values[valueIndex++];
            result.replace(start, end + this.placeholderSuffix.length(), replaceContent);
            start = result.indexOf(this.placeholderPrefix, start + replaceContent.length());
        }
        return result.toString();
    }

    /**
     * 解析带有指定占位符的模板字符串 <br/>
     * <pre>
     *  content = "category:${}:product:${}"
     *  values = {"1", "2"}
     *  output: "category:1:product:2"
     * </pre>
     *
     * @param content 要解析的带有占位符的模板字符串
     * @param values   按照模板占位符索引位置设置对应的值
     * @return  解析的字符串
     */
    public String resolve(String content, Object[] values) {
        return resolve(content, Stream.of(values).map(String::valueOf).toArray(String[]::new));
    }

    /**
     * 根据替换规则来替换指定模板中的占位符值
     * @param content  要解析的字符串
     * @param rule  解析规则回调
     * @return 解析的字符串
     */
    public String resolveByRule(String content, Function<String, String> rule) {
        int start = content.indexOf(this.placeholderPrefix);
        if (start == -1) {
            return content;
        }
        StringBuilder result = new StringBuilder(content);
        while (start != -1) {
            int end = result.indexOf(this.placeholderSuffix, start);
            // 获取占位符属性值，如${id}, 即获取id
            String placeholder = result.substring(start + this.placeholderPrefix.length(), end);
            // 替换整个占位符内容，即将${id}值替换为替换规则回调中的内容
            String replaceContent = placeholder.trim().isEmpty() ? "" : rule.apply(placeholder);
            if (replaceContent == null) replaceContent = "";
            result.replace(start, end + this.placeholderSuffix.length(), replaceContent);
            start = result.indexOf(this.placeholderPrefix, start + replaceContent.length());
        }
        return result.toString();
    }

    /**
     * 替换模板中占位符内容，占位符的内容即为map，key为占位符中的内容。<br/>
     * <pre>
     *  content = "product:${id}:detail:${did}"
     *  valueMap = {"id": 1, "pid": 2}
     *  output: "product:1:detail:2"
     * </pre>
     *
     * @param content  模板内容
     * @param valueMap 值映射
     * @return   替换完成后的字符串
     */
    public String resolveByMap(String content, final Map<String, Object> valueMap) {
        return resolveByRule(content, placeholderValue -> String.valueOf(valueMap.get(placeholderValue)));
    }

    /**
     * 根据properties文件替换占位符内容
     * @param content       模板内容
     * @param properties    Properties
     * @return  替换完成后的字符串
     */
    public String resolveByProperties(String content, final Properties properties) {
        return resolveByRule(content, properties::getProperty);
    }

    /**
     * 根据对象中字段路径(即类似js访问对象属性值)替换模板中的占位符 <br/>
     * <pre>
     *  content = "product:${id}:detail:${detail.id}"
     *  obj = Product.builder().id(1).detail(Detail.builder().id(2).build()).build();
     *  output: "product:1:detail:2"
     * </pre>
     *
     * @param content  要解析的内容
     * @param obj   填充解析内容的对象(如果是基本类型，则所有占位符替换为相同的值)
     * @return  替换完成后的字符串
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String resolveByObject(String content, final Object obj) {
        if (obj instanceof Map) {
            return resolveByMap(content, (Map) obj);
        }
        return resolveByRule(content, placeholderValue -> {
            try {
                Field declaredField = obj.getClass().getDeclaredField(placeholderValue);
                declaredField.setAccessible(true);
                return String.valueOf(declaredField.get(obj));
            } catch (Exception e) {
                log.error("Invoke field from obj[{}] error: {}", obj, e.getMessage(), e);
            }
            return null;
        });
    }
}