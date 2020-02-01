package com.github.yizzuide.milkomeda.util;

import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.io.StringReader;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * MybatisUtil
 *
 * @author yizzuide
 * @since 2.5.0
 * Create at 2020/01/30 20:34
 */
public class MybatisUtil {
    private static CCJSqlParserManager sqlParserManager = new CCJSqlParserManager();
    private static TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
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
}
