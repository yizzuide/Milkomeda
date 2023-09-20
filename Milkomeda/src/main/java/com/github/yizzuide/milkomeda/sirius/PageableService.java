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
import com.github.yizzuide.milkomeda.sirius.wormhole.SiriusInspector;
import com.github.yizzuide.milkomeda.universe.context.AopContextHolder;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.util.DataTypeConvertUtil;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
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
        Map<String, MappingNode> mappingFields = new HashMap<>();
        // 需要在linker关联结果过滤条件
        Map<String, LinkNode> filterMap = new HashMap<>();
        for (Field field : fields) {
            // collect query linker
            Set<QueryLinker> queryLinkers = AnnotatedElementUtils.getMergedRepeatableAnnotations(field, QueryLinker.class, QueryLinkers.class);
            if (!CollectionUtils.isEmpty(queryLinkers)) {
                linkerFields.add(field);

                // collect mappings
                QueryLinker mappingQl = queryLinkers.stream().filter(ql -> StringUtils.isNotEmpty(ql.mappings())).findFirst().orElse(null);
                if (mappingQl != null) {
                    String[] mappings;
                    if (mappingQl.mappings().contains(",")) {
                        mappings = mappingQl.mappings().split(",");
                    } else {
                        mappings = new String[]{ mappingQl.mappings() };
                    }
                    Arrays.stream(mappings)
                            .map(StringUtils::strip)
                            .map(m -> m.split("\\s*->\\s*"))
                            .forEach(m -> {
                                String targetFieldName = m[0];
                                String fieldName = m[1];
                                Object fieldValue = tableInfo.getPropertyValue(target, targetFieldName);
                                MappingNode mappingNode = new MappingNode();
                                mappingNode.setFieldName(fieldName);
                                mappingNode.setFieldValue(fieldValue);
                                mappingNode.setPrefectType(PrefectType.EQ);
                                TableFieldInfo fieldInfo = tableInfo.getFieldList().stream().filter(fi -> fi.getField().getName().equals(targetFieldName)).findFirst().orElse(null);
                                if (fieldInfo != null) {
                                    QueryMatcher mappingQueryMatcher = AnnotationUtils.findAnnotation(fieldInfo.getField(), QueryMatcher.class);
                                    if (mappingQueryMatcher != null) {
                                        mappingNode.setPrefectType(mappingQueryMatcher.prefect());
                                    }
                                }
                                mappingFields.put(fieldName, mappingNode);
                            });
                }
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
                        fieldValue = findLinkerValue(queryMatcher, linkerFields, queryPageData.getEntity(), field, tableInfo, filterMap);
                        fieldNonNull = fieldValue != null;
                    }
                    queryWrapper.eq(fieldNonNull, columnName, fieldValue);
                } else if (queryMatcher.prefect() == PrefectType.NEQ) {
                    queryWrapper.ne(fieldNonNull, columnName, fieldValue);
                } else if (queryMatcher.prefect() == PrefectType.EMPTY) {
                    queryWrapper.eq(ObjectUtils.isEmpty(fieldValue), columnName, fieldValue);
                } else if (queryMatcher.prefect() == PrefectType.LIKE) {
                    queryWrapper.like(fieldNonNull && StringUtils.isNotBlank(fieldValue.toString()), columnName, fieldValue);
                } else if (queryMatcher.prefect() == PrefectType.IN || queryMatcher.prefect() == PrefectType.LINK_EQ_IN) {
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
                        valueObjects = (Collection<Object>) findLinkerValue(queryMatcher, linkerFields, queryPageData.getEntity(), field, tableInfo, filterMap);
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

        // 设置查询字段
        List<String> includeColumns = new ArrayList<>();
        List<String> excludeColumns = new ArrayList<>();
        for (TableFieldInfo tableFieldInfo : tableInfo.getFieldList()) {
            QueryFieldInclude fieldInclude = AnnotationUtils.findAnnotation(tableFieldInfo.getField(), QueryFieldInclude.class);
            if (fieldInclude != null && ArrayUtils.contains(fieldInclude.group(), group)) {
                includeColumns.add(tableFieldInfo.getColumn());
                continue;
            }
            QueryFieldExclude fieldExclude = AnnotationUtils.findAnnotation(tableFieldInfo.getField(), QueryFieldExclude.class);
            if (fieldExclude != null && ArrayUtils.contains(fieldExclude.group(), group)) {
                excludeColumns.add(tableFieldInfo.getColumn());
            }
        }
        if (!includeColumns.isEmpty() && !includeColumns.contains(tableInfo.getKeyColumn())) {
            includeColumns.add(tableInfo.getKeyColumn());
        }
        if (!includeColumns.isEmpty() || !excludeColumns.isEmpty()) {
            queryWrapper.select(getEntityClass(), fi -> {
                if (includeColumns.contains(fi.getColumn())) {
                    return true;
                }
                return !excludeColumns.contains(fi.getColumn());
            });
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
                        .sorted(OrderComparator.INSTANCE.withSourceProvider(t -> t))
                        .collect(Collectors.toList());
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

        // query link name
        if (!CollectionUtils.isEmpty(linkerFields) && !CollectionUtils.isEmpty(records)) {
            for (Field linkerField : linkerFields) {
                // cache link entity list with the field
                Map<String, List<?>> linkEntityListCacheMap = new HashMap<>();
                Set<QueryLinker> queryLinkers = AnnotatedElementUtils.getMergedRepeatableAnnotations(linkerField, QueryLinker.class, QueryLinkers.class);
                for (QueryLinker queryLinker : queryLinkers) {
                    if (!Arrays.asList(queryLinker.group()).contains(group)) {
                        continue;
                    }
                    BaseMapper linkMapper = SiriusInspector.getMapper(queryLinker.linkEntityType());
                    List<?> linkEntityList;
                    String linkMapperClassName = linkMapper.getClass().getName();
                    // get link entity table info
                    TableInfo linkTableInfo = TableInfoHelper.getTableInfo(queryLinker.linkEntityType());
                    if (linkEntityListCacheMap.containsKey(linkMapperClassName)) {
                        linkEntityList = linkEntityListCacheMap.get(linkMapperClassName);
                    } else {
                        Set<Serializable> idValues = records.stream()
                                .map(e -> (Serializable) tableInfo.getPropertyValue(e, linkerField.getName()))
                                .filter(e -> !queryLinker.linkIdIgnore().equals(e.toString()))
                                .collect(Collectors.toSet());
                        if (CollectionUtils.isEmpty(idValues)) {
                            continue;
                        }
                        QueryWrapper<T> linkQueryWrapper = new QueryWrapper<>();
                        boolean isSameIdNamed = queryLinker.linkIdField().equals(linkTableInfo.getKeyColumn());
                        String linkIdColumn = isSameIdNamed ? queryLinker.linkIdField() : findColumnName(linkTableInfo, queryLinker.linkIdField());
                        linkQueryWrapper.in(linkIdColumn, idValues);
                        // add condition of mappings
                        if (!CollectionUtils.isEmpty(mappingFields)) {
                            mappingFields.forEach((mappingFieldName, mappingNode) -> {
                                String colName = findColumnName(linkTableInfo ,mappingFieldName);
                                Object mappingFieldValue = mappingNode.getFieldValue();
                                if (mappingNode.getPrefectType() == PrefectType.EQ) {
                                    linkQueryWrapper.eq(colName, mappingFieldValue);
                                } else if (mappingNode.getPrefectType() == PrefectType.NEQ) {
                                    linkQueryWrapper.ne(colName, mappingFieldValue);
                                } else if (mappingNode.getPrefectType() == PrefectType.EMPTY) {
                                    linkQueryWrapper.eq(ObjectUtils.isEmpty(mappingFieldValue), colName, mappingFieldValue);
                                }  else if (mappingNode.getPrefectType() == PrefectType.IN) {
                                    linkQueryWrapper.in(colName, mappingFieldValue);
                                } else if (mappingNode.getPrefectType() == PrefectType.LIKE) {
                                    linkQueryWrapper.like(colName, mappingFieldValue);
                                }
                            });
                        }

                        // 如果有需要过滤的link字段
                        if (!filterMap.isEmpty()) {
                            for (String key : filterMap.keySet()) {
                                LinkNode linkNode = filterMap.get(key);
                                String linkNameColumn = findColumnName(linkTableInfo, linkNode.getLinkFieldName());
                                if (linkNode.getPrefectType() == PrefectType.IN) {
                                    linkQueryWrapper.like(linkNameColumn, linkNode.getTargetFieldValue());
                                } else {
                                    linkQueryWrapper.eq(linkNameColumn, linkNode.getTargetFieldValue());
                                }
                            }
                        }

                        // 设置查询的字段
                        List<String> queryColumns = new ArrayList<>();
                        queryColumns.add(linkTableInfo.getKeyColumn());
                        // 所有当前类型的linkId和LinkName
                        List<String> linkColumns = queryLinkers.stream()
                                .filter(ql -> ql.linkEntityType() == queryLinker.linkEntityType())
                                .map(ql -> new String[] { ql.linkIdField(), ql.linkNameField() })
                                .flatMap(Arrays::stream)
                                .distinct()
                                .filter(f -> !queryColumns.contains(f))
                                .map(f -> findColumnName(linkTableInfo, f))
                                .collect(Collectors.toList());
                        queryColumns.addAll(linkColumns);
                        linkQueryWrapper.select(queryColumns.toArray(new String[]{}));
                        linkEntityList = linkMapper.selectList(linkQueryWrapper);
                        linkEntityListCacheMap.put(linkMapperClassName, linkEntityList);
                    }
                    if (!CollectionUtils.isEmpty(linkEntityList)) {
                        for (T record : records) {
                            for (Object le : linkEntityList) {
                                Object linkId = linkTableInfo.getPropertyValue(le, queryLinker.linkIdField());
                                Object nameValue = linkTableInfo.getPropertyValue(le, queryLinker.linkNameField());
                                Object matchIdValue = tableInfo.getPropertyValue(record, linkerField.getName());
                                if (String.valueOf(matchIdValue).equals(String.valueOf(linkId))) {
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

    @SuppressWarnings("unchecked")
    private Object findLinkerValue(QueryMatcher queryMatcher, List<Field> linkerFields, Object entity, Field field, TableInfo tableInfo, Map<String, LinkNode> filterMap) {
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
            Class<?> linkClass = matchQueryLinker.linkEntityType();
            TableInfo linkTableInfo = TableInfoHelper.getTableInfo(linkClass);
            if (linkTableInfo == null) {
                return null;
            }

            // add this linker field name and value as condition for the link query
            LinkNode linkNode = new LinkNode();
            linkNode.setPrefectType(queryMatcher.prefect());
            linkNode.setTargetFieldValue(searchValue);
            linkNode.setTargetFieldName(matchQueryLinker.targetNameField());
            linkNode.setLinkFieldName(matchQueryLinker.linkNameField());
            filterMap.put(matchQueryLinker.linkNameField(), linkNode);
            // find link entity with linker mapper
            BaseMapper<T> linkMapper = (BaseMapper<T>) SiriusInspector.getMapper(linkClass);
            String linkNameColumn = findColumnName(linkTableInfo, matchQueryLinker.linkNameField());
            QueryWrapper<T> queryExample = new QueryWrapper<>();
            if (queryMatcher.prefect() == PrefectType.IN || queryMatcher.prefect() == PrefectType.LINK_EQ_IN) {
                if (queryMatcher.prefect() == PrefectType.IN) {
                    queryExample.like(linkNameColumn, searchValue);
                } else {
                    queryExample.eq(linkNameColumn, searchValue);
                }
                String linkIdColumn =findColumnName(linkTableInfo, matchQueryLinker.linkIdField());
                queryExample.select(linkTableInfo.getKeyColumn(), linkNameColumn, linkIdColumn);
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
                queryExample.eq(linkNameColumn, searchValue);
                queryExample.select(linkTableInfo.getKeyColumn(), linkNameColumn);
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

    @Data
    static class LinkNode {
        private PrefectType prefectType;
        private Object targetFieldValue;
        private String targetFieldName;
        private String linkFieldName;
    }

    @Data
    static class MappingNode {
        private PrefectType prefectType;
        private Object fieldValue;
        private String fieldName;
    }
}
