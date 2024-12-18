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

package com.github.yizzuide.milkomeda.universe.parser.yml;

import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformHandler;
import com.github.yizzuide.milkomeda.universe.extend.env.CollectionsPropertySource;
import com.github.yizzuide.milkomeda.util.DataTypeConvertUtil;
import lombok.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * YmlResponseOutput
 * 统一的response节点输出
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.14.0
 * <br>
 * Create at 2020/04/10 14:15
 */
public class YmlResponseOutput {

    public static final String STATUS = "status";
    public static final String CODE = "code";
    public static final String MESSAGE = "message";
    public static final String DATA = "data";
    public static final String ADDITION = "addition";
    public static final String CUSTOMS = "customs";
    public static final String CLAZZ =  "clazz";
    public static final String ERROR_STACK_MSG = "error-stack-msg";
    public static final String ERROR_STACK = "error-stack";

    /**
     * response节点输出处理
     * @param nodeMap           源信息节点
     * @param result            结果Map
     * @param defValMap         默认值源
     * @param e                 异常
     * @param customException   是否是自定义异常
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void output(@NonNull Map<String, Object> nodeMap, @NonNull Map<String, Object> result, Map<String, Object> defValMap, @Nullable Exception e, boolean customException) {
        // 自定义异常基本信息写出
        if (e != null && customException) {
            Map<String, Object> exMap = DataTypeConvertUtil.beanToMap(e);
            // 如果yml有默认配置，重置到异常message
            Object messageDefaultValue = YmlParser.extractAliasNode(nodeMap, MESSAGE).getT2();
            if ((messageDefaultValue != null && StringUtils.hasText(messageDefaultValue.toString()))
                    && (exMap.get(MESSAGE) != null && StringUtils.hasText(exMap.get(MESSAGE).toString()))) {
                exMap.put(MESSAGE, messageDefaultValue);
            }
            // 自定义异常的信息值使用自定义异常属性值替换
            YmlParser.parseAliasMapPath(nodeMap, result, CODE, null, exMap);
            YmlParser.parseAliasMapPath(nodeMap, result, MESSAGE, null, exMap);
            // 其它自定义key通过自定义异常属性写出
            nodeMap.keySet().stream().filter(k -> !Arrays.asList(CLAZZ, STATUS, CODE, MESSAGE, ADDITION).contains(k) && !result.containsKey(k))
                    .forEach(k ->  YmlParser.parseAliasMapPath(nodeMap, result, k, null, exMap));
        } else { // 非自定义异常基本信息写出，支持默认值源
            YmlParser.parseAliasMapPath(nodeMap, result, CODE, defValMap == null ? -1 : defValMap.get(CODE), null);
            YmlParser.parseAliasMapPath(nodeMap, result, MESSAGE, defValMap == null ? "Server internal error！" : defValMap.get(MESSAGE), null);
            YmlParser.parseAliasMapPath(nodeMap, result, DATA, null, null);
        }
        // 内部异常详情写出
        if (e != null && !customException) {
            // 自定义异常栈的详情值从内部异常解析出来，不需要默认值，不指定不会返回
            YmlParser.parseAliasMapPath(nodeMap, result, ERROR_STACK_MSG, null, e.getMessage());
            List<StackTraceElement> stackTraceElements = Arrays.stream(e.getStackTrace()).limit(5).collect(Collectors.toList());
            String errorStack = org.apache.commons.lang3.StringUtils.join(stackTraceElements, '\n');
            YmlParser.parseAliasMapPath(nodeMap, result, ERROR_STACK, null, errorStack);
        }
        // 附加字段写出
        Object addition = nodeMap.get(ADDITION);
        if (addition instanceof Map) {
            Map<String, Object> additionMap = (Map) addition;
            for (String key : additionMap.keySet()) {
                additionMap.compute(key, (k, ele) -> CollectionsPropertySource.of(String.valueOf(ele)));
            }
            result.putAll(additionMap);
        }
        UniformHandler.resultFilter(result);
    }
}
