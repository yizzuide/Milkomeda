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

package com.github.yizzuide.milkomeda.util;

import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * IOUtils
 *
 * @author yizzuide
 * @since 3.3.1
 * @version 3.15.0
 * <br>
 * Create at 2020/05/07 14:17
 */
public class IOUtils {
    public static final String LUA_PATH = "/META-INF/scripts";

    /**
     * 加载lua脚本
     * @param path      所在路径
     * @param filename  文件名（包含扩展名）
     * @return          lua脚本文本
     * @throws IOException 读取异常
     * @since 3.3.1
     */
    public static String loadLua(String path, String filename) throws IOException {
        if (path == null || filename == null) {
            return "";
        }
        InputStream inputStream = IOUtils.class.getResourceAsStream(path + "/" + filename);
        if (inputStream == null) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            // 去注释
            if (line.matches("\\s*-{2,}.*")) {
                continue;
            }
            // 去空行
            if (line.matches("\\s+") || line.matches("^\\s*[\\r\\n]?")) {
                continue;
            }
            // 代码缩减空白
            line = StringUtils.trimLeadingWhitespace(line);
            line = StringUtils.trimTrailingWhitespace(line);
            line = line.replaceAll("[\n\r\t]", "");
            out.append(line);
            // 忽略有分号的行，说明已有结束符
            if (line.endsWith(";")) {
                continue;
            }
            // 指令行，添加空格
            if (line.startsWith("if") || line.startsWith("then") || line.endsWith("else") || line.startsWith("for") || line.startsWith("do")
                || line.startsWith("while") || line.startsWith("function")) {
                out.append(" ");
            } else {
                // 语句行，添加结束符
                out.append("; ");
            }
        }
        return out.toString();
    }
}
