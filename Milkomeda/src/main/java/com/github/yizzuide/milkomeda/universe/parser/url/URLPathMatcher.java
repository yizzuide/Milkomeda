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

package com.github.yizzuide.milkomeda.universe.parser.url;

import com.github.yizzuide.milkomeda.universe.context.WebContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.PathContainer;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.util.ServletRequestPathUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * URI路径匹配器
 *
 * @see org.springframework.util.AntPathMatcher
 * @see org.springframework.web.util.pattern.PathPatternParser
 * @since 3.0.0
 * @version 4.0.0
 * @author yizzuide
 * Create at 2020/03/29 14:07
 */
public class URLPathMatcher {

    private final static List<String> wildSymbols = Arrays.asList("*", "/**");

    /**
     * 获取匹配的无限定路径
     * @return  List
     */
    public static List<String> getMatchWildSymbols() {
        return wildSymbols;
    }

    /**
     * 获取设置的无限定路径
     * @return  List
     */
    public static List<String> getWildSymbols() {
        return Collections.singletonList(wildSymbols.get(1));
    }

    /**
     * 匹配路径
     * @param sourcePaths   源路径列表
     * @param targetPath    目标路径（如果为空，从request中获取)
     * @return  匹配是否成功
     */
    public static boolean match(@NonNull List<String> sourcePaths, @Nullable String targetPath) {
        if (targetPath == null) {
            HttpServletRequest request = WebContext.getRequestNonNull();
            if (ServletRequestPathUtils.hasCachedPath(request)) {
                targetPath = ServletRequestPathUtils.getCachedPathValue(request);
            }
        }
        String finalPath = targetPath;
        if (finalPath == null) {
            return false;
        }
        // Spring Boot 2.6: Using PathPatternParser to match.
        return sourcePaths.stream().anyMatch(pathPattern -> WebContext.getMvcPatternParser().parse(pathPattern).matches(PathContainer.parsePath(finalPath)));

        // Simple impl...
        /*boolean matched = false;
        for (String path : sourcePaths) {
            if (StringExtensionsKt.isEmpty(path)) continue;
            // 去除最后一个/
            String lastChar = path.substring(path.length() - 1);
            if (path.length() > 1 && "/".equals(lastChar)) {
                path = path.substring(0, path.length() - 1);
            }

            // 多路径匹配（以/**结尾）
            boolean subPathWild = path.length() > 2 && "/**".equals(path.substring(path.length() - 3));
            if (subPathWild) {
                String pathPrefix = path.substring(0, path.length() - 3);
                if (targetPath.startsWith(pathPrefix)) {
                    matched = true;
                    break;
                }
            }

            // 一个子路径匹配（以/*结尾）
            boolean wordWild = "*".equals(lastChar);
            if (wordWild) {
                String pathPrefix = path.substring(0, path.length() - 2);
                if (targetPath.startsWith(pathPrefix)) {
                    matched = true;
                    break;
                }
            }

            // 完全路径匹配
            if (targetPath.equals(path)) {
                matched = true;
                break;
            }
        }
        return matched;*/
    }
}
