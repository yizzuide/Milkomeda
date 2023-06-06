package com.github.yizzuide.milkomeda.sirius;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformPage;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformQueryPageData;
import com.github.yizzuide.milkomeda.universe.context.AopContextHolder;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.util.DataTypeConvertUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A pageable service impl extends from ServiceImpl.
 *
 * @since 3.14.0
 * @author yizzuide
 * <br>
 * Create at 2022/10/30 14:59
 */
public class PageableService<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> implements IPageableService<T> {

    @Override
    public M getBaseMapper() {
        return super.getBaseMapper();
    }

    @SuppressWarnings("unchecked")
    protected Class<M> currentMapperClass() {
        return (Class<M>) ReflectionKit.getSuperClassGenericType(this.getClass(), PageableService.class, 0);
    }

    @SuppressWarnings("unchecked")
    protected Class<T> currentModelClass() {
        return (Class<T>) ReflectionKit.getSuperClassGenericType(this.getClass(), PageableService.class, 1);
    }

    @SuppressWarnings("ConstantConditions")
    public UniformPage<T> selectByPage(UniformQueryPageData<T> queryPageData) {
        Page<T> page = new Page<>();
        page.setCurrent(queryPageData.getPageStart());
        page.setSize(queryPageData.getPageSize());

        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        Class<T> tClass = currentModelClass();
        T target = queryPageData.getEntity();
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClass);
        Field[] fields = tClass.getDeclaredFields();
        boolean postOrderByASC = true;
        boolean needPostOrderBy = false;
        Field orderField = null;
        for (Field field : fields) {
            QueryMatcher queryMatcher = field.getDeclaredAnnotation(QueryMatcher.class);
            if (queryMatcher == null) {
                continue;
            }
            String columnName = null;
            // find from table info
            for (TableFieldInfo tableFieldInfo : tableInfo.getFieldList()) {
                if (field.getName().equals(tableFieldInfo.getProperty())) {
                    columnName = tableFieldInfo.getColumn();
                    break;
                }
            }
            // get from @TableField or convert it
            if (columnName == null) {
                TableField tableField = field.getDeclaredAnnotation(TableField.class);
                if (tableField != null) {
                    columnName = tableField.value();
                } else {
                    columnName = DataTypeConvertUtil.humpToLine(field.getName());
                }
            }

            Object fieldValue = null;
            if (target != null) {
                // can get field value from getter method？
                tableInfo.getPropertyValue(target, field.getName());
                // reflect it!
                if (fieldValue == null) {
                    ReflectionUtils.makeAccessible(field);
                    fieldValue = ReflectionUtils.getField(field, target);
                }
            }

            boolean fieldNonNull = Objects.nonNull(target) && Objects.nonNull(fieldValue);
            if (queryMatcher.prefect() == PrefectType.EQ) {
                queryWrapper.eq(fieldNonNull, columnName, fieldValue);
            } else if (queryMatcher.prefect() == PrefectType.NEQ) {
                queryWrapper.ne(fieldNonNull, columnName, fieldValue);
            } else if (queryMatcher.prefect() == PrefectType.LIKE) {
                    queryWrapper.like(fieldNonNull && StringUtils.isNotBlank(fieldValue.toString()), columnName, fieldValue);
            } else if (queryMatcher.prefect() == PrefectType.PageDate) {
                queryWrapper.ge(Objects.nonNull(queryPageData.getStartDate()), columnName, queryPageData.getStartDate());
                queryWrapper.le(Objects.nonNull(queryPageData.getEndDate()), columnName, queryPageData.getEndDate());
            } else if (queryMatcher.prefect() == PrefectType.OrderByPre) {
                queryWrapper.orderBy(true, queryMatcher.forward(), columnName);
            } else if (queryMatcher.prefect() == PrefectType.OrderByPost) {
                needPostOrderBy = true;
                postOrderByASC = queryMatcher.forward();
                orderField = field;
            } else {
                additionParseQueryMatcher(queryWrapper, queryMatcher.prefectString(), columnName, fieldNonNull, fieldValue);
            }
        }

        Page<T> recordPage = this.baseMapper.selectPage(page, queryWrapper);
        UniformPage<T> uniformPage = new UniformPage<>();
        uniformPage.setTotalSize(recordPage.getTotal());
        uniformPage.setPageCount(recordPage.getPages());
        List<T> records = recordPage.getRecords();
        if (!CollectionUtils.isEmpty(records) && needPostOrderBy) {
            // impl of Ordered
            if (target instanceof Ordered) {
                records = records.stream()
                        .sorted(OrderComparator.INSTANCE.withSourceProvider(t -> t)).collect(Collectors.toList());
            } else {
                Field finalOrderField = orderField;
                records.sort(Comparator.comparingInt(t -> {
                    // can get field value from getter method？
                    Object propertyValue = tableInfo.getPropertyValue(t, finalOrderField.getName());
                    if (propertyValue != null) {
                        return (Integer) propertyValue;
                    }
                    // reflect it!
                    return (Integer) ReflectionUtils.getField(finalOrderField, t);
                }));
            }
            // need desc?
            if (!postOrderByASC) {
                Collections.reverse(records);
            }
        }
        uniformPage.setList(records);
        return uniformPage;
    }

    /**
     * Addition parse query matcher with {@link QueryMatcher#prefectString()}
     * @param queryWrapper      QueryWrapper
     * @param prefectString     type for match query
     * @param columnName        table column name
     * @param isFieldNonNull    false if field value
     * @param fieldValue         entity field value
     */
    protected void additionParseQueryMatcher(QueryWrapper<T> queryWrapper, String prefectString, String columnName, boolean isFieldNonNull, Object fieldValue) {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public boolean removeBeforeCheckRef(T entityWithID) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClass);
        Object id = tableInfo.getPropertyValue(entityWithID, tableInfo.getKeyProperty());
        // check RefMatcher on entity field
        for (Field field : entityWithID.getClass().getDeclaredFields()) {
            RefMatcher refMatcher = field.getDeclaredAnnotation(RefMatcher.class);
            if (refMatcher == null) {
                continue;
            }
            if (refMatcher.type() == RefMatcher.RefType.SELF) {
                String columnName = null;
                // find from table info
                for (TableFieldInfo tableFieldInfo : tableInfo.getFieldList()) {
                    if (field.getName().equals(tableFieldInfo.getProperty())) {
                        columnName = tableFieldInfo.getColumn();
                        break;
                    }
                }
                QueryWrapper<T> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq(columnName, id);
                Long count = this.baseMapper.selectCount(queryWrapper);
                if (count > 0) {
                    return false;
                }
                break;
            }
        }
        // check RefMatcher on mapper type
        RefMatcher refMatcher = this.mapperClass.getDeclaredAnnotation(RefMatcher.class);
        if(refMatcher != null &&  BaseMapper.class.isAssignableFrom(refMatcher.foreignMapper())) {
            QueryWrapper foreignQueryWrapper = new QueryWrapper();
            foreignQueryWrapper.eq(refMatcher.foreignField(), id);
            BaseMapper referenceMapper = (BaseMapper) ApplicationContextHolder.get().getBean(refMatcher.foreignMapper());
            if (referenceMapper.selectCount(foreignQueryWrapper) > 0) {
                return false;
            }
        }
        AopContextHolder.self(this.getClass()).removeById((Serializable) id);
        return true;
    }

    @SuppressWarnings("unchecked")
    public boolean assignAuthority(Serializable ownerId, List<? extends Serializable> itemIds, SFunction<T, Object> conditionProvider, Function<Serializable, T> generator) {
        // 删除原有分配（如果不是通过LambdaQueryWrapper执行操作，创建时不添加this.baseMapper）
        LambdaQueryWrapper<T> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(conditionProvider, ownerId);
        AopContextHolder.self(this.getClass()).remove(lambdaQueryWrapper);

        // 添加新的分配
        if(CollectionUtils.isEmpty(itemIds)) {
            return true;
        }
        List<T> sysUserRoles = itemIds.stream().map(generator).collect(Collectors.toList());
        return AopContextHolder.self(this.getClass()).saveBatch(sysUserRoles);
    }
}
