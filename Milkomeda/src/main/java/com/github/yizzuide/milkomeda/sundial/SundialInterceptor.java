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

package com.github.yizzuide.milkomeda.sundial;

import com.github.yizzuide.milkomeda.universe.engine.el.SimpleElParser;
import com.github.yizzuide.milkomeda.util.MybatisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.CollectionUtils;
import com.github.yizzuide.milkomeda.util.Strings;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * SundialInterceptor
 *
 * @author yizzuide
 * @since 3.8.0
 * @version 3.12.10
 * Create at 2020/06/16 11:18
 */
@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})
public class SundialInterceptor implements Interceptor {

    @Autowired
    private SundialProperties props;

    @Lazy
    @Resource
    private ShardingFunction shardingFunction;

    private final Map<String, Map<String, Method>> cacheMap = new HashMap<>();

    private static final Pattern WHITE_SPACE_BLOCK_PATTERN = Pattern.compile("([\\s]{2,}|[\\t\\r\\n])");

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        String methodId = ms.getId();

        // 是否使用了分库分表策略
        Sundial sundial = getMethod(methodId).getAnnotation(Sundial.class);
        if (sundial == null || sundial.shardingType() == ShardingType.NONE) {
            return invocation.proceed();
        }

        String nodeExp = sundial.nodeExp();
        String partExp = sundial.partExp();
        // 未设置节点拆分和表拆分，直接返回
        if (Strings.isEmpty(nodeExp) && Strings.isEmpty(partExp)) {
            return invocation.proceed();
        }

        // 没有可拆分的ShardingKey，直接返回
        Object params = args.length > 1 ? args[1] : null;
        if (params == null) {
            return invocation.proceed();
        }
        BoundSql boundSql = ms.getSqlSource().getBoundSql(params);
        String sql = WHITE_SPACE_BLOCK_PATTERN.matcher(boundSql.getSql()).replaceAll(" ");
        String tableName = MybatisUtil.getFirstTableName(sql);
        assert tableName != null;

        ShardingRoot root = new ShardingRoot();
        root.setTable(tableName);
        root.setP(params);
        root.setFn(shardingFunction);
        String schema = null;
        // 包含分库的处理
        if (sundial.shardingType() != ShardingType.TABLE && !Strings.isEmpty(nodeExp)) {
            String node = SimpleElParser.parse(nodeExp, root, String.class);
            SundialProperties.DataNode dataNode = props.getSharding().getNodes().get(node);
            // node_001 --转--> node_1
            if (dataNode == null) {
                int separatorIndex = node.lastIndexOf(props.getSharding().getIndexSeparator());
                String index = node.substring(separatorIndex + 1);
                node = node.substring(0, separatorIndex) + props.getSharding().getIndexSeparator() + Integer.parseInt(index);
                dataNode = props.getSharding().getNodes().get(node);
            }
            // 是否使用主连接
            if (sundial.key().equals(DynamicRouteDataSource.MASTER_KEY)) {
                SundialHolder.setDataSourceType(dataNode.getLeader());
            } else if(sundial.key().equals("follows")) { // 从库任选
                if (!CollectionUtils.isEmpty(dataNode.getFollows())) {
                    SundialHolder.setDataSourceType(dataNode.getFollows().stream().findAny().orElse(DynamicRouteDataSource.MASTER_KEY));
                }
            } else {
                SundialHolder.setDataSourceType(sundial.key());
            }
            // 需要添加的数据库名
            if (!Strings.isEmpty(dataNode.getSchema())) {
                schema = dataNode.getSchema();
            }
            // 如果仅为分库类型
            if (sundial.shardingType() == ShardingType.SCHEMA) {
                // sql不需要修改，直接返回
                if (schema == null) {
                    return invocation.proceed();
                }
                // 加入数据库名
                sql = sql.replace(tableName, schema + "." + tableName);
                updateSql(sql, invocation, ms, args, boundSql);
                return invocation.proceed();
            }
        }

        // 分库分表 or 分表
        if (!Strings.isEmpty(partExp)) {
            String part = SimpleElParser.parse(partExp, root, String.class);
            // 如果保留原表名，去掉0索引后缀
            if (props.getSharding().isOriginalNameAsIndexZero()) {
                int separatorIndex = part.lastIndexOf(props.getSharding().getIndexSeparator());
                String index = part.substring(separatorIndex + 1);
                if (Integer.parseInt(index) == 0) {
                    part = part.substring(0, separatorIndex);
                }
            }
            // 如果sql不需要修改，直接返回
            if (tableName.equals(part) && schema == null) {
                return invocation.proceed();
            }
            sql = sql.replace(tableName, schema == null ? part : schema + "." + part);
            updateSql(sql, invocation, ms, args, boundSql);
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            // 为当前target创建动态代理
            return Plugin.wrap(target, this);
        }
        return target;
    }

    private Method getMethod(String methodId) throws ClassNotFoundException {
        int partIndex = methodId.lastIndexOf('.');
        String className = methodId.substring(0, partIndex);
        String methodName = methodId.substring(partIndex + 1);
        cacheMap.computeIfAbsent(className, k -> new HashMap<>());
        Map<String, Method> methodMap = cacheMap.get(className);
        Method foundMethod = methodMap.get(methodName);
        if (foundMethod != null) {
            return foundMethod;
        }
        Class<?> clazz = Class.forName(className);
        Method method = null;
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            if (declaredMethod.getName().equals(methodName)) {
                method = declaredMethod;
            }
        }
        assert method != null;
        methodMap.put(methodName, method);
        return method;
    }

    private void updateSql(String sql, Invocation invocation, MappedStatement ms, Object[] args, BoundSql boundSql) {
        BoundSql boundSqlNew = new BoundSql(ms.getConfiguration(), sql, boundSql.getParameterMappings(), boundSql.getParameterObject());
        MappedStatement mappedStatement = copyFrom(ms, new BoundSqlSqlSource(boundSqlNew));
        // 替换映射的语句
        args[0] = mappedStatement;

        // 针对查询方式的参数替换
        if (ms.getSqlCommandType() == SqlCommandType.SELECT) {
            Executor executor = (Executor) invocation.getTarget();
            Object parameter = args[1];
            RowBounds rowBounds = (RowBounds) args[2];
            // 6个参数时（因为分页插件的原因导致问题，需要修改对应的类型值）
            if (args.length == 6) {
                args[4] = executor.createCacheKey(ms, parameter, rowBounds, boundSql);
                args[5] = boundSqlNew;
            }
        }
    }

    private MappedStatement copyFrom(MappedStatement ms, SqlSource newSqlSource) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), newSqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length > 0) {
            builder.keyProperty(org.apache.commons.lang3.StringUtils.join(ms.getKeyProperties(), ','));
        }
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.resultOrdered(ms.isResultOrdered());
        builder.resultSetType(ms.getResultSetType());
        if (ms.getKeyColumns() != null && ms.getKeyColumns().length > 0) {
            builder.keyColumn(org.apache.commons.lang3.StringUtils.join(ms.getKeyColumns(), ','));
        }
        builder.databaseId(ms.getDatabaseId());
        builder.lang(ms.getLang());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());
        return builder.build();
    }

    public static class BoundSqlSqlSource implements SqlSource {
        private final BoundSql boundSql;

        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }
}
