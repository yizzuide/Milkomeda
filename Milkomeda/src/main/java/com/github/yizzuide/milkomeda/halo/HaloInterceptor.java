package com.github.yizzuide.milkomeda.halo;

import com.github.yizzuide.milkomeda.universe.metadata.HandlerMetaData;
import com.github.yizzuide.milkomeda.util.MybatisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Method;
import java.util.List;

/**
 * HaloInterceptor
 * 拦截器插件
 *
 * @author yizzuide
 * @since 2.5.0
 * @version 2.5.1
 * Create at 2020/01/30 20:38
 */
@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})
public class HaloInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        // 获取第一个参数，MappedStatement
        MappedStatement mappedStatement = (MappedStatement) args[0];
        // 获取第二个参数，该参数类型根据Mapper方法的参数决定，如果是一个参数，则为实体或简单数据类型；如果是多个参数，则为Map。
        Object param = args.length > 1 ? args[1] : null;
        String sql = mappedStatement.getSqlSource().getBoundSql(param).getSql();
        List<String> tableNames = MybatisUtil.getTableNames(sql);
        String tableName = tableNames.get(0);
        // 匹配所有
        invokeWithTable(tableName,"*", mappedStatement, param, null, HaloType.PRE);
        // 完全匹配
        invokeWithTable(tableName, tableName, mappedStatement, param, null, HaloType.PRE);
        Object result = invocation.proceed();
        // 匹配所有
        invokeWithTable(tableName, "*", mappedStatement, param, result, HaloType.POST);
        // 完全匹配
        invokeWithTable(tableName, tableName, mappedStatement, param, result, HaloType.POST);
        return result;
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    private void invokeWithTable(String tableName, String matchTableName, MappedStatement mappedStatement, Object param, Object result, HaloType type) {
        HaloContext.getTableNameMap().get(matchTableName).stream()
                .filter(metaData -> metaData.getAttributes().get(HaloContext.ATTR_TYPE) == type)
                .forEach(handlerMetaData -> invokeHandler(tableName, handlerMetaData, mappedStatement, param, result));
    }

    private void invokeHandler(String tableName, HandlerMetaData handlerMetaData, MappedStatement mappedStatement, Object param, Object result) {
        try {
            // INSERT/UPDATE/DELETE/SELECT
            SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
            Method method = handlerMetaData.getMethod();
            Object target = handlerMetaData.getTarget();
            // 获取参数类型
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 1 && parameterTypes[0] == HaloMeta.class) {
                HaloMeta haloMeta = new HaloMeta(sqlCommandType, tableName, param, result);
                method.invoke(target, haloMeta);
            } else if (parameterTypes.length > 1) {
                // 检测参数是否有SqlCommandType
                if (parameterTypes[0] == SqlCommandType.class) {
                    if (result == null) {
                        method.invoke(target, sqlCommandType, param);
                    } else {
                        method.invoke(target, sqlCommandType, param, result);
                    }
                } else {
                    if (result == null) {
                        method.invoke(target, param, sqlCommandType);
                    } else {
                        method.invoke(target, param, result, sqlCommandType);
                    }
                }
            } else {
                method.invoke(target, param);
            }
        } catch (Exception e) {
            log.error("Halo invoke handler [{}] error: {}, with stmt id: {}", handlerMetaData.getTarget(), e.getMessage(), mappedStatement.getId(), e);
        }
    }
}
