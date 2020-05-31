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
