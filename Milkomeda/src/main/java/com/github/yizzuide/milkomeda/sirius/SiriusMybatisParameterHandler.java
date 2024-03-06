/*
 * Copyright (c) 2022 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.sirius;

import com.baomidou.mybatisplus.core.MybatisParameterHandler;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import org.apache.ibatis.executor.statement.PreparedStatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.util.StringUtils;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Auto interpolate extension of {@link MybatisParameterHandler}.
 *
 * @since 3.15.0
 * @version 4.0.0
 * @author yizzuide
 * <br>
 * Create at 2022/12/02 19:03
 * @see PreparedStatementHandler
 * @see MybatisParameterHandler#setParameters(PreparedStatement)
 */
public class SiriusMybatisParameterHandler extends MybatisParameterHandler {

    private SiriusConfig.MybatisPlusMetaObjectHandler metaObjectHandler;

    public SiriusMybatisParameterHandler(MappedStatement mappedStatement, Object parameter, BoundSql boundSql) {
        super(mappedStatement, parameter, boundSql);

        if (!this.getMetaObjectHandler().getProps().isAutoAddFill() ||
                SqlCommandType.SELECT == mappedStatement.getSqlCommandType()) {
            return;
        }

        String ignoreLogicDelete = this.getMetaObjectHandler().getProps().getIgnoreLogicDelete();
        if ( (StringUtils.hasLength(ignoreLogicDelete) &&
                boundSql.getSql().contains(String.format("SET %s", ignoreLogicDelete)))) {
            return;
        }

        boolean findFlag = false;
        Object entity = null;
        if (parameter instanceof Map<?, ?> map) {
            if (map.containsKey(Constants.ENTITY)) {
               entity = map.get(Constants.ENTITY);
            }
        } else {
            entity = parameter;
        }
        if (entity != null) {
            TableInfo tableInfo = TableInfoHelper.getTableInfo(entity.getClass());
            if (tableInfo == null) {
                return;
            }
            for (TableFieldInfo tableFieldInfo : tableInfo.getFieldList()) {
                if (mappedStatement.getSqlCommandType() == SqlCommandType.INSERT) {
                    if (this.getMetaObjectHandler().getInsertFields().contains(tableFieldInfo.getProperty())) {
                        findFlag = true;
                        break;
                    }
                } else {
                    if (this.getMetaObjectHandler().getUpdateFields().contains(tableFieldInfo.getProperty())) {
                        findFlag = true;
                        break;
                    }
                }
            }
        }

        if (!findFlag) {
            return;
        }

        // 根据值填充后的模型重新构建BoundSql -> PreparedStatementHandler.instantiateStatement() -> PreparedStatementHandler.parameterize()
        BoundSql newBoundSql = mappedStatement.getBoundSql(getParameterObject());
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings == null || parameterMappings.isEmpty()) {
            return;
        }
        parameterMappings.clear();
        parameterMappings.addAll(newBoundSql.getParameterMappings());
        Map<String, Object> props = new HashMap<>();
        props.put("sql", newBoundSql.getSql());
        ReflectUtil.setField(boundSql, props);
    }

    @Override
    protected void insertFill(MetaObject metaObject, TableInfo tableInfo) {
        if (this.getMetaObjectHandler().getProps().isAutoAddFill() && !tableInfo.isWithInsertFill()) {
            this.getMetaObjectHandler().conditionFill(true, tableInfo);
        }
        super.insertFill(metaObject, tableInfo);
    }

    @Override
    protected void updateFill(MetaObject metaObject, TableInfo tableInfo) {
        if (this.getMetaObjectHandler().getProps().isAutoAddFill() && !tableInfo.isWithUpdateFill()) {
            this.getMetaObjectHandler().conditionFill(false, tableInfo);
        }
        super.updateFill(metaObject, tableInfo);
    }

    public SiriusConfig.MybatisPlusMetaObjectHandler getMetaObjectHandler() {
        if (this.metaObjectHandler == null) {
            this.metaObjectHandler = ApplicationContextHolder.get().getBean(SiriusConfig.MybatisPlusMetaObjectHandler.class);
        }
        return metaObjectHandler;
    }
}
