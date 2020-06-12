package com.github.yizzuide.milkomeda.halo;

import com.github.yizzuide.milkomeda.pulsar.PulsarHolder;
import com.github.yizzuide.milkomeda.universe.metadata.HandlerMetaData;
import com.github.yizzuide.milkomeda.util.MybatisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.defaults.DefaultSqlSession;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * HaloInterceptor
 * 拦截器插件
 *
 * @author yizzuide
 * @since 2.5.0
 * @version 3.7.2
 * Create at 2020/01/30 20:38
 */
@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})
public class HaloInterceptor implements Interceptor {

    private static final Pattern WHITE_SPACE_BLOCK_PATTERN = Pattern.compile("([\\s]{2,}|[\\t\\r\\n])");

    @Autowired
    private HaloProperties props;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        // 获取第一个参数，MappedStatement
        MappedStatement mappedStatement = (MappedStatement) args[0];
        // 获取第二个参数，该参数类型根据Mapper方法的参数决定，如果是一个参数，则为实体或简单数据类型；如果是多个参数，则为Map。
        Object param = args.length > 1 ? args[1] : null;
        BoundSql boundSql = mappedStatement.getSqlSource().getBoundSql(param);
        String sql = WHITE_SPACE_BLOCK_PATTERN.matcher(boundSql.getSql()).replaceAll(" ");
        if (!props.isShowSlowLog()) {
            return warpIntercept(invocation, mappedStatement, sql, param);
        }
        long start = System.currentTimeMillis();
        Object result = warpIntercept(invocation, mappedStatement, sql, param);
        long end = System.currentTimeMillis();
        long time = end - start;
        if (time > props.getSlowThreshold().toMillis()) {
            logSqlInfo(mappedStatement.getConfiguration(), boundSql, sql, mappedStatement.getId(), time);
        }
        return result;
    }

    // 打印Sql日志
    private void logSqlInfo(Configuration configuration, BoundSql boundSql, String sql, String sqlId, long time) {
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        List<String> params = new ArrayList<>();
        if (parameterMappings.size() > 0 && parameterObject != null) {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                params.add(getParameterValue(parameterObject));
            } else {
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        params.add(getParameterValue(obj));
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        params.add(getParameterValue(obj));
                    }
                }
            }
        }
        log.warn("Halo found slow sql[{}]: {} params:[{}], take time: {}ms", sqlId, sql, StringUtils.join(params, ','), time);
    }

    private String getParameterValue(Object obj) {
        String value;
        if (obj instanceof String) {
            value = "'" + obj.toString() + "'";
        } else if (obj instanceof Date) {
            Instant instant = ((Date) obj).toInstant();
            String format = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            value = "'" + format + "'";
        } else {
            if (obj != null) {
                value = obj.toString();
            } else {
                value = "";
            }
        }
        return value;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object warpIntercept(Invocation invocation, MappedStatement mappedStatement, String sql, Object param) throws Throwable {
        // 如果处理器为空，直接返回
        if (CollectionUtils.isEmpty(HaloContext.getTableNameMap())) {
            return invocation.proceed();
        }
        String tableName = MybatisUtil.getFirstTableName(sql);
        if (StringUtils.isEmpty(tableName)) {
            return invocation.proceed();
        }

        // Mybatis map参数获取不存在时会抛异常，转为普通map
        if (param instanceof DefaultSqlSession.StrictMap) {
            param = new HashMap((Map) param);
        }

        // 匹配所有
        invokeWithTable(tableName,"*", sql, mappedStatement, param, null, HaloType.PRE);
        // 完全匹配
        invokeWithTable(tableName, tableName, sql, mappedStatement, param, null, HaloType.PRE);
        Object result = invocation.proceed();
        // 匹配所有
        invokeWithTable(tableName, "*", sql, mappedStatement, param, result, HaloType.POST);
        // 完全匹配
        invokeWithTable(tableName, tableName, sql, mappedStatement, param, result, HaloType.POST);
        return result;
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            // 为当前target创建动态代理
            return Plugin.wrap(target, this);
        }
        return target;
    }

    private void invokeWithTable(String tableName, String matchTableName, String sql, MappedStatement mappedStatement, Object param, Object result, HaloType type) {
        if (!HaloContext.getTableNameMap().containsKey(matchTableName)) {
            return;
        }
        HaloContext.getTableNameMap().get(matchTableName).stream()
                .filter(metaData -> metaData.getAttributes().get(HaloContext.ATTR_TYPE) == type)
                .forEach(handlerMetaData -> {
                    if ((boolean) handlerMetaData.getAttributes().get(HaloContext.ATTR_ASYNC)) {
                        PulsarHolder.getPulsar().post(() -> invokeHandler(tableName, handlerMetaData, sql, mappedStatement, param, result));
                    } else {
                        invokeHandler(tableName, handlerMetaData, sql, mappedStatement, param, result);
                    }
                });
    }

    private void invokeHandler(String tableName, HandlerMetaData handlerMetaData, String sql, MappedStatement mappedStatement, Object param, Object result) {
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
            log.error("Halo invoke handler [{}] error: {}, with stmt id: {} and sql: {}", handlerMetaData.getTarget(), e.getMessage(), mappedStatement.getId(), sql, e);
        }
    }

    @Override
    public void setProperties(Properties properties) {
        // 插件注册的属性处理
    }
}
