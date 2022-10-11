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

import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.util.StringUtils;

import java.io.StringReader;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * MybatisUtil
 *
 * @author yizzuide
 * @since 2.5.0
 * @version 3.7.1
 * <br>
 * Create at 2020/01/30 20:34
 */
public class MybatisUtil {
    private static final CCJSqlParserManager sqlParserManager = new CCJSqlParserManager();
    private static final TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
    /**
     * 获得真正的处理对象,可能多层代理
     * @param target    代理目标
     * @param <T>       目标类型
     * @return  被代理的真实对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T realTarget(Object target) {
        if (Proxy.isProxyClass(target.getClass())) {
            MetaObject metaObject = SystemMetaObject.forObject(target);
            return realTarget(metaObject.getValue("h.target"));
        }
        return (T) target;
    }

    /**
     * 获取sql里的表名
     * @param sql   sql语句
     * @return  表名
     * @throws Exception    获取异常
     */
    public static List<String> getTableNames(String sql) throws Exception {
        Statement statement = sqlParserManager.parse(new StringReader(sql));
        return tablesNamesFinder.getTableList(statement);
    }

    /**
     * 获取第一个表名
     * @param sql   sql语句
     * @return  表名
     * @since 3.7.0
     */
    public static String getFirstTableName(String sql) {
        sql = StringUtils.trimLeadingWhitespace(sql.toLowerCase());
        if (sql.startsWith("select") || sql.startsWith("delete")) {
            return extractTableName(sql, " from ");
        }
        if (sql.startsWith("insert")) {
            return extractTableName(sql, " into ");
        }
        if (sql.startsWith("update")) {
            return extractTableName(sql, "update ");
        }
        return null;
    }

    private static String extractTableName(String sql, String from) {
        int fromStart = sql.indexOf(from);
        if (fromStart == -1) {
            return null;
        }
        int fromEnd = fromStart + from.length();
        int tableEnd = sql.indexOf(" ", fromEnd);
        String tableName = tableEnd == -1 ? sql.substring(fromEnd) : sql.substring(fromEnd, tableEnd);
        if (from.equals(" into ") && tableName.contains("(")) {
            tableName = tableName.substring(0, tableName.indexOf("("));
            if (tableName.endsWith(" ")) {
                tableName = tableName.substring(0, tableName.length() - 1);
            }
        }
        if (tableName.startsWith("`")) {
            tableName = tableName.substring(1);
        }
        if (tableName.endsWith("`")) {
            tableName = tableName.substring(0, tableName.length() - 1);
        }
        return tableName;
    }
}
