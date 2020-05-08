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
 * @since 3.4.0
 * Create at 2020/05/07 14:17
 */
public class IOUtils {

    /**
     * 加载lua脚本
     * @param path      所在路径
     * @param filename  文件名（包含扩展名）
     * @return          lua脚本文本
     * @throws IOException 读取异常
     * @since 3.4.0
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
            if (line.startsWith("--")) {
                continue;
            }
            line = StringUtils.trimLeadingWhitespace(line);
            line = StringUtils.trimLeadingWhitespace(line);
            line = line.replaceAll("[\n\r\t]", "");
            out.append(line);
            if (line.startsWith("if") || line.startsWith("then") || line.endsWith("else") || line.startsWith("for") || line.startsWith("do")
                || line.startsWith("while") || line.startsWith("function")) {
                out.append(" ");
            } else {
                out.append("; ");
            }
        }
        return out.toString();
    }
}
