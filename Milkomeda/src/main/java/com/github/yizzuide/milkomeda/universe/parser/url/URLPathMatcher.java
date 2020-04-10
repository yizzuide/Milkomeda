package com.github.yizzuide.milkomeda.universe.parser.url;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * URLPathMatcher
 * 路径匹配器
 *
 * @author yizzuide
 * @since 3.0.0
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
     * @param targetPath    目标路径
     * @return  匹配是否成功
     */
    public static boolean match(List<String> sourcePaths, String targetPath) {
        boolean matched = false;
        for (String path : sourcePaths) {
            if (StringUtils.isEmpty(path)) continue;
            // 去除最后一个/
            String lastChar = path.substring(path.length() - 1);
            if (path.length() > 1 && "/".equals(lastChar)) {
                path = path.substring(0, path.length() - 1);
            }

            // 多路径匹配
            boolean subPathWild = path.length() > 2 && "/**".equals(path.substring(path.length() - 3));
            if (subPathWild) {
                String pathPrefix = path.substring(0, path.length() - 3);
                if (targetPath.startsWith(pathPrefix)) {
                    matched = true;
                    break;
                }
            }

            // 一个子路径匹配
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
        return matched;
    }
}
