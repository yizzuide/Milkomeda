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
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A pageable service impl extends from ServiceImpl.
 *
 * @since 3.14.0
 * @version 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2022/10/30 14:59
 */
public class PageableService<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> implements IPageableService<T> {

    /**
     * Query matcher default group name.
     */
    public static final String DEFAULT_GROUP = "default";

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

    public UniformPage<T> selectByPage(UniformQueryPageData<T> queryPageData) {
        return selectByPage(queryPageData, DEFAULT_GROUP);
    }

    public UniformPage<T> selectByPage(UniformQueryPageData<T> queryPageData, String group) {
        return selectByPage(queryPageData, null, group);
    }

    public UniformPage<T> selectByPage(UniformQueryPageData<T> queryPageData, Map<String, Object> queryMatchData) {
        return selectByPage(queryPageData, queryMatchData, DEFAULT_GROUP);
    }

    @SuppressWarnings({"ConstantConditions", "rawtypes", "unchecked"})
    public UniformPage<T> selectByPage(UniformQueryPageData<T> queryPageData, Map<String, Object> queryMatchData, String group) {
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
        List<Field> linkerFields = new ArrayList<>();
        for (Field field : fields) {
            // collect query linker
            Set<QueryLinker> queryLinkers = AnnotatedElementUtils.getMergedRepeatableAnnotations(field, QueryLinker.class, QueryLinkers.class);
            if (!CollectionUtils.isEmpty(queryLinkers)) {
                linkerFields.add(field);
            }

            Set<QueryMatcher> queryMatchers = AnnotatedElementUtils.getMergedRepeatableAnnotations(field, QueryMatcher.class, QueryMatchers.class);
            if (CollectionUtils.isEmpty(queryMatchers)) {
                continue;
            }
            for (QueryMatcher queryMatcher : queryMatchers) {
                if (!Arrays.asList(queryMatcher.group()).contains(group)) {
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
                    }
                    if (StringUtils.isEmpty(columnName)) {
                        columnName = DataTypeConvertUtil.humpToLine(field.getName());
                    }
                }

                Object fieldValue = null;
                if (target != null) {
                    // can get field value from getter method？
                    fieldValue = tableInfo.getPropertyValue(target, field.getName());
                    // reflect it!
                    if (fieldValue == null) {
                        ReflectionUtils.makeAccessible(field);
                        fieldValue = ReflectionUtils.getField(field, target);
                    }
                }

                boolean fieldNonNull = Objects.nonNull(target) && Objects.nonNull(fieldValue);
                if (queryMatcher.prefect() == PrefectType.EQ) {
                    if (!fieldNonNull && StringUtils.isNotEmpty(queryMatcher.matchDataField())) {
                        fieldValue = findLinkerValue(queryMatcher, linkerFields, queryPageData.getEntity(), field, tableInfo);
                        fieldNonNull = fieldValue != null;
                    }
                    queryWrapper.eq(fieldNonNull, columnName, fieldValue);
                } else if (queryMatcher.prefect() == PrefectType.NEQ) {
                    queryWrapper.ne(fieldNonNull, columnName, fieldValue);
                } else if (queryMatcher.prefect() == PrefectType.LIKE) {
                    queryWrapper.like(fieldNonNull && StringUtils.isNotBlank(fieldValue.toString()), columnName, fieldValue);
                } else if (queryMatcher.prefect() == PrefectType.IN) {
                    Collection<Object> valueObjects = Collections.singletonList(fieldValue);
                    boolean condition = fieldNonNull;
                    if (queryMatchData != null) {
                        Object values = queryMatchData.get(field.getName());
                        if (values instanceof Object[]) {
                            valueObjects = Arrays.asList((Object[]) values);
                            condition = true;
                        }
                    }
                    if (!condition && StringUtils.isNotEmpty(queryMatcher.matchDataField())) {
                        valueObjects = (Collection<Object>) findLinkerValue(queryMatcher, linkerFields, queryPageData.getEntity(), field, tableInfo);
                        condition = valueObjects != null;
                    }
                    queryWrapper.in(condition, columnName, valueObjects);
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
        }

        UniformPage<T> uniformPage = new UniformPage<>();
        List<T> records;
        // 如果页记录数为-1，则不分页
        if (page.getSize() == -1) {
            Long totalSize = this.baseMapper.selectCount(queryWrapper);
            records = this.baseMapper.selectList(queryWrapper);
            uniformPage.setTotalSize(totalSize);
            uniformPage.setPageCount(1L);
        } else {
            Page<T> recordPage = this.baseMapper.selectPage(page, queryWrapper);
            records = recordPage.getRecords();
            uniformPage.setTotalSize(recordPage.getTotal());
            uniformPage.setPageCount(recordPage.getPages());
        }
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

        // handle query link name
        if (!CollectionUtils.isEmpty(linkerFields) && !CollectionUtils.isEmpty(records)) {
            for (Field linkerField : linkerFields) {
                Set<QueryLinker> queryLinkers = AnnotatedElementUtils.getMergedRepeatableAnnotations(linkerField, QueryLinker.class, QueryLinkers.class);
                for (QueryLinker queryLinker : queryLinkers) {
                    if (!Arrays.asList(queryLinker.group()).contains(group)) {
                        continue;
                    }
                    Set<Serializable> idValues = records.stream()
                            .map(e -> (Serializable) tableInfo.getPropertyValue(e, linkerField.getName()))
                            .filter(e -> !Long.valueOf(e.toString()).equals(queryLinker.linkIdIgnore()))
                            .collect(Collectors.toSet());
                    if (CollectionUtils.isEmpty(idValues)) {
                        continue;
                    }
                    BaseMapper linkMapper = ApplicationContextHolder.get().getBean(queryLinker.linkMapper());
                    QueryWrapper<T> linkQueryWrapper = new QueryWrapper<>();
                    linkQueryWrapper.in(queryLinker.linkIdField(), idValues);
                    List<?> linkEntityList = linkMapper.selectList(linkQueryWrapper);
                    if (!CollectionUtils.isEmpty(linkEntityList)) {
                        // get link entity table info
                        TableInfo LinkTableInfo = TableInfoHelper.getTableInfo(linkEntityList.get(0).getClass());
                        for (T record : records) {
                            for (Object le : linkEntityList) {
                                Object id = LinkTableInfo.getPropertyValue(le, queryLinker.linkIdField());
                                Object nameValue = LinkTableInfo.getPropertyValue(le, queryLinker.linkNameField());
                                Object matchIdValue = tableInfo.getPropertyValue(record, linkerField.getName());
                                if (String.valueOf(matchIdValue).equals(String.valueOf(id))) {
                                    tableInfo.setPropertyValue(record, queryLinker.targetNameField(), nameValue);
                                    break;
                                }
                            }
                        }
                    }
                }

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
                String columnName = findColumnName(tableInfo, field.getName());
                QueryWrapper<T> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq(columnName, id);
                Long count = this.baseMapper.selectCount(queryWrapper);
                if (count > 0) {
                    return false;
                }
                break;
            }
        }
        // check RefMatcher has annotated on a mapper
        RefMatcher refMatcher = this.mapperClass.getDeclaredAnnotation(RefMatcher.class);
        if(refMatcher != null &&  BaseMapper.class.isAssignableFrom(refMatcher.foreignMapper())) {
            QueryWrapper foreignQueryWrapper = new QueryWrapper();
            foreignQueryWrapper.eq(refMatcher.foreignField(), id);
            BaseMapper referenceMapper = ApplicationContextHolder.get().getBean(refMatcher.foreignMapper());
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
        List<T> Authorities = itemIds.stream().map(generator).collect(Collectors.toList());
        return AopContextHolder.self(this.getClass()).saveBatch(Authorities);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object findLinkerValue(QueryMatcher queryMatcher, List<Field> linkerFields, Object entity, Field field, TableInfo tableInfo) {
        Object searchValue = tableInfo.getPropertyValue(entity, queryMatcher.matchDataField());
        if (searchValue == null) {
            return null;
        }
        for (Field linkerField : linkerFields) {
            // find linker field which matches `matchDataField` from QueryMatcher
            Optional<QueryLinker> queryLinkerOptional = AnnotatedElementUtils.getMergedRepeatableAnnotations(linkerField, QueryLinker.class, QueryLinkers.class).stream()
                    .filter(linker -> linker.targetNameField().equals(queryMatcher.matchDataField())).findFirst();
            if (!queryLinkerOptional.isPresent()) {
                continue;
            }
            QueryLinker matchQueryLinker = queryLinkerOptional.get();
            Class<? extends BaseMapper> linkMapperClass = matchQueryLinker.linkMapper();
            // find a generic type of BaseMapper<Type>
            Class<?> linkClass = ResolvableType.forClass(linkMapperClass).getInterfaces()[0].getGenerics()[0].resolve();
            TableInfo linkTableInfo = TableInfoHelper.getTableInfo(linkClass);
            if (linkTableInfo == null) {
                return null;
            }
            // find link entity with linker mapper
            String column = findColumnName(linkTableInfo, matchQueryLinker.linkNameField());
            BaseMapper linkMapper = ApplicationContextHolder.get().getBean(linkMapperClass);
            QueryWrapper<?> queryExample = new QueryWrapper<>();
            if (queryMatcher.prefect() == PrefectType.IN) {
                queryExample.like(column, searchValue);
                List<?> linkRecordlist = linkMapper.selectList(queryExample);
                if (CollectionUtils.isEmpty(linkRecordlist)) {
                    return genNonFoundValue(field, queryMatcher.prefect());
                }
                // collect and return link entity id list
                return linkRecordlist.stream()
                        .map(record -> linkTableInfo.getPropertyValue(record, matchQueryLinker.linkIdField()))
                        .collect(Collectors.toSet());
            }
            if (queryMatcher.prefect() == PrefectType.EQ) {
                queryExample.eq(column, searchValue);
                Object linkRecord = linkMapper.selectOne(queryExample);
                if (linkRecord == null) {
                    return genNonFoundValue(field, queryMatcher.prefect());
                }
                return linkTableInfo.getPropertyValue(linkRecord, matchQueryLinker.linkIdField());
            }
        }
        return null;
    }

    private Object genNonFoundValue(Field field, PrefectType type) {
        if (field.getType() == Integer.class || field.getType() == Long.class) {
            return type == PrefectType.EQ ? -1 : Collections.singletonList(-1);
        }
        if (field.getType() == String.class) {
            return type == PrefectType.EQ ? "-1" : Collections.singletonList("-1");
        }
        return null;
    }

    private String findColumnName(TableInfo tableInfo, String fieldName) {
        for (TableFieldInfo tableFieldInfo : tableInfo.getFieldList()) {
            if (fieldName.equals(tableFieldInfo.getProperty())) {
                return tableFieldInfo.getColumn();
            }
        }
        return null;
    }
}
