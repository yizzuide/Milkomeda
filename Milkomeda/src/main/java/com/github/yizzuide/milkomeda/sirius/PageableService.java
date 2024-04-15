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
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformPage;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformQueryPageData;
import com.github.yizzuide.milkomeda.sirius.wormhole.SiriusInspector;
import com.github.yizzuide.milkomeda.universe.context.AopContextHolder;
import com.github.yizzuide.milkomeda.util.DataTypeConvertUtil;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A pageable service impl extends from ServiceImpl.
 *
 * @since 3.14.0
 * @version 3.20.0
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
    @Override
    protected Class<M> currentMapperClass() {
        return (Class<M>) ReflectionKit.getSuperClassGenericType(this.getClass(), PageableService.class, 0);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Class<T> currentModelClass() {
        return (Class<T>) ReflectionKit.getSuperClassGenericType(this.getClass(), PageableService.class, 1);
    }

    public UniformPage<T> selectByPage(UniformQueryPageData<T> queryPageData) {
        return selectByPage(queryPageData, DEFAULT_GROUP);
    }

    public UniformPage<T> selectByPage(UniformQueryPageData<T> queryPageData, String group) {
        return selectByPage(queryPageData, (Map<String, Object>) null, group);
    }

    public UniformPage<T> selectByPage(UniformQueryPageData<T> queryPageData, Map<String, Object> queryMatchData) {
        return selectByPage(queryPageData, queryMatchData, DEFAULT_GROUP);
    }

    public UniformPage<T> selectByPage(UniformQueryPageData<T> queryPageData,
                                       Map<String, Object> queryMatchData,
                                       String group) {
        return selectByPage(queryPageData, queryMatchData, null, group);
    }

    public <V> UniformPage<V> selectByPage(UniformQueryPageData<T> queryPageData,
                                       Function<T, V> entity2VoConverter,
                                       String group) {
        return selectByPage(queryPageData, null, entity2VoConverter, group);
    }

    @SuppressWarnings({"ConstantConditions", "rawtypes", "unchecked"})
    public <V> UniformPage<V> selectByPage(UniformQueryPageData<T> queryPageData,
                                       Map<String, Object> queryMatchData,
                                       Function<T, V> entity2VoConverter,
                                       String group) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        Class<T> tClass = currentModelClass();
        T target = queryPageData.getEntity();
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClass);
        Field[] fields = TypeReflector.getFields(tClass);
        List<Field> linkerFields = new ArrayList<>();
        Map<String, Set<QueryLinkerNode>> linkerNodes = new HashMap<>();
        // 需要在linker关联结果过滤条件
        Map<String, PrefectLinkNode> filterMap = new HashMap<>();
        for (Field field : fields) {
            // convert QueryAutoLinker to QueryLinkerNode
            Set<QueryAutoLinker> queryAutoLinkers = AnnotatedElementUtils.getMergedRepeatableAnnotations(field, QueryAutoLinker.class, QueryAutoLinkers.class);
            if (!CollectionUtils.isEmpty(queryAutoLinkers)) {
                List<QueryAutoLinker> filteredQueryAutoLinkers = queryAutoLinkers.stream()
                        .filter(ql -> Arrays.asList(ql.group()).contains(group))
                        .collect(Collectors.toList());
                for (QueryAutoLinker queryAutoLinker : filteredQueryAutoLinkers) {
                    Set<QueryLinkerNode> nodes = QueryLinkerNode.build(queryAutoLinker);
                    linkerNodes.put(field.getName(), nodes);
                    linkerFields.add(field);
                }
            }

            // convert QueryLinker to QueryLinkerNode
            Set<QueryLinker> queryLinkers = AnnotatedElementUtils.getMergedRepeatableAnnotations(field, QueryLinker.class, QueryLinkers.class);
            if (!CollectionUtils.isEmpty(queryLinkers)) {
                Set<QueryLinker> filteredQueryLinkers = queryLinkers.stream()
                        .filter(ql -> Arrays.asList(ql.group()).contains(group))
                        .collect(Collectors.toSet());
                Set<QueryLinkerNode> nodes = QueryLinkerNode.build(filteredQueryLinkers);
                linkerNodes.put(field.getName(), nodes);
                linkerFields.add(field);
            }

            Set<QueryMatcher> queryMatchers = AnnotatedElementUtils.getMergedRepeatableAnnotations(field, QueryMatcher.class, QueryMatchers.class);
            if (CollectionUtils.isEmpty(queryMatchers)) {
                continue;
            }
            List<QueryMatcher> filteredQueryMatchers = queryMatchers.stream()
                    .filter(qm -> Arrays.asList(qm.group()).contains(group))
                    .collect(Collectors.toList());
            for (QueryMatcher queryMatcher : filteredQueryMatchers) {
                if (queryMatcher.prefect() == PrefectType.OrderBy) {
                    continue;
                }
                String columnName = findColumnName(tableInfo, field);
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
                        fieldValue = findLinkerValue(linkerNodes, queryMatcher, linkerFields, queryPageData.getEntity(), field, tableInfo, filterMap);
                        fieldNonNull = fieldValue != null;
                    }
                    queryWrapper.eq(fieldNonNull, columnName, fieldValue);
                } else if (queryMatcher.prefect() == PrefectType.NEQ) {
                    queryWrapper.ne(fieldNonNull, columnName, fieldValue);
                } else if (queryMatcher.prefect() == PrefectType.GT) {
                    queryWrapper.gt(fieldNonNull, columnName, fieldValue);
                } else if (queryMatcher.prefect() == PrefectType.GE) {
                    queryWrapper.ge(fieldNonNull, columnName, fieldValue);
                } else if (queryMatcher.prefect() == PrefectType.LT) {
                    queryWrapper.lt(fieldNonNull, columnName, fieldValue);
                } else if (queryMatcher.prefect() == PrefectType.LE) {
                    queryWrapper.le(fieldNonNull, columnName, fieldValue);
                } else if (queryMatcher.prefect() == PrefectType.EMPTY) {
                    queryWrapper.eq(ObjectUtils.isEmpty(fieldValue), columnName, fieldValue);
                } else if (queryMatcher.prefect() == PrefectType.LIKE) {
                    queryWrapper.like(fieldNonNull && StringUtils.isNotBlank(fieldValue.toString()), columnName, fieldValue);
                } else if (queryMatcher.prefect() == PrefectType.IN || queryMatcher.prefect() == PrefectType.LINK_EQ_IN) {
                    Collection<Object> valueObjects = Collections.singletonList(fieldValue);
                    boolean condition = fieldNonNull;
                    if (queryMatchData != null) {
                        Object values = queryMatchData.get(field.getName());
                        if (values != null) {
                            condition = true;
                        }
                        if (values instanceof Collection) {
                            valueObjects = (Collection<Object>) values;
                        } else if (values instanceof Object[]) {
                            valueObjects = Arrays.asList((Object[]) values);
                        }
                    }
                    if (!condition && StringUtils.isNotEmpty(queryMatcher.matchDataField())) {
                        valueObjects = (Collection<Object>) findLinkerValue(linkerNodes, queryMatcher, linkerFields, queryPageData.getEntity(), field, tableInfo, filterMap);
                        condition = !CollectionUtils.isEmpty(valueObjects);
                    }
                    queryWrapper.in(condition, columnName, valueObjects);
                } else if (queryMatcher.prefect() == PrefectType.PageDate) {
                    queryWrapper.ge(Objects.nonNull(queryPageData.getStartDate()), columnName, queryPageData.getStartDate());
                    queryWrapper.le(Objects.nonNull(queryPageData.getEndDate()), columnName, queryPageData.getEndDate());
                } else if (queryMatcher.prefect() == PrefectType.PageUnixTime) {
                    queryWrapper.ge(Objects.nonNull(queryPageData.getStartDate()), columnName, queryPageData.getStartUnixTime());
                    queryWrapper.le(Objects.nonNull(queryPageData.getEndDate()), columnName, queryPageData.getEndUnixTime());
                } else {
                    additionParseQueryMatcher(queryWrapper, queryMatcher.prefectString(), columnName, fieldNonNull, fieldValue);
                }
            }
            filteredQueryMatchers.stream()
                    .filter(qm -> qm.prefect() == PrefectType.OrderBy)
                    .sorted(Comparator.comparingInt(QueryMatcher::order))
                    .forEach(queryMatcher -> queryWrapper.orderBy(true, queryMatcher.forward(), findColumnName(tableInfo, field)));
        }

        // 设置查询字段
        Set<String> includeColumns = new HashSet<>();
        Set<String> excludeColumns = new HashSet<>();
        for (TableFieldInfo tableFieldInfo : tableInfo.getFieldList()) {
            QueryFieldInclude fieldInclude = AnnotationUtils.findAnnotation(tableFieldInfo.getField(), QueryFieldInclude.class);
            if (fieldInclude != null && ArrayUtils.contains(fieldInclude.group(), group)) {
                includeColumns.add(tableFieldInfo.getColumn());
                continue;
            }
            QueryFieldExclude fieldExclude = AnnotationUtils.findAnnotation(tableFieldInfo.getField(), QueryFieldExclude.class);
            if (fieldExclude != null && (Arrays.asList(fieldExclude.group()).contains("*") || ArrayUtils.contains(fieldExclude.group(), group))) {
                excludeColumns.add(tableFieldInfo.getColumn());
            }
        }
        boolean hasSelectColumn = !includeColumns.isEmpty();
        if (hasSelectColumn) {
            // 添加主键字段
            includeColumns.add(tableInfo.getKeyColumn());
            // 添加linker字段
            includeColumns.addAll(linkerFields.stream().map(field -> findColumnName(tableInfo, field.getName())).collect(Collectors.toSet()));
        }

        Predicate<TableFieldInfo> selectFieldPredicate = fi -> {
            if (excludeColumns.contains(fi.getColumn())) {
                return false;
            }
            return includeColumns.contains(fi.getColumn());
        };
        if (hasSelectColumn || !excludeColumns.isEmpty()) {
            queryWrapper.select(getEntityClass(), selectFieldPredicate);
        }

        UniformPage<V> uniformPage = new UniformPage<>();
        List<T> records;
        List<V> voList = null;
        // 如果页记录数为-1，则不分页
        if (queryPageData.getPageSize() == -1) {
            if (hasSelectColumn) {
                queryWrapper.select(tableInfo.getKeyColumn());
            }
            Long totalSize = this.baseMapper.selectCount(queryWrapper);
            if (hasSelectColumn) {
                queryWrapper.select(getEntityClass(), selectFieldPredicate);
            }
            records = this.baseMapper.selectList(queryWrapper);
            uniformPage.setTotalSize(totalSize);
            uniformPage.setPageCount(1L);
        } else {
            //Page<T> resultPage = this.baseMapper.selectPage(page, queryWrapper);
            try (com.github.pagehelper.Page<T> pageable = PageHelper.startPage(queryPageData.getPageStart(), queryPageData.getPageSize())) {
                pageable.toPageInfo().setList(this.baseMapper.selectList(queryWrapper));
                records = pageable.getResult();
                uniformPage.setTotalSize(pageable.getTotal());
                uniformPage.setPageCount((long) pageable.getPages());
            }
        }

        // query link name
        if (!CollectionUtils.isEmpty(linkerFields) && !CollectionUtils.isEmpty(records)) {
            for (Field linkerField : linkerFields) {
                // collect mappings
                Map<String, MappingNode> mappingFields = new HashMap<>();
                if (!linkerNodes.isEmpty() && linkerNodes.containsKey(linkerField.getName())) {
                    MappingNode.construct(linkerNodes.get(linkerField.getName()), mappingFields, tableInfo, target);
                }

                // cache link entity list with the field
                Map<String, List<?>> linkEntityListCacheMap = new HashMap<>();
                List<QueryLinkerNode> queryLinkerNodes = linkerNodes.get(linkerField.getName()).stream()
                        .sorted(Comparator.comparingInt(QueryLinkerNode::getLinkSelectTop)).collect(Collectors.toList());
                for (QueryLinkerNode linkerNode : queryLinkerNodes) {
                    BaseMapper linkMapper = SiriusInspector.getMapper(linkerNode.getLinkEntityType());
                    List<?> linkEntityList;
                    // get link entity table info
                    TableInfo linkTableInfo = TableInfoHelper.getTableInfo(linkerNode.getLinkEntityType());
                    String linkMapperClassName = linkTableInfo.getCurrentNamespace();
                    if (linkEntityListCacheMap.containsKey(linkMapperClassName)) {
                        linkEntityList = linkEntityListCacheMap.get(linkMapperClassName);
                    } else {
                        Set<Serializable> idValues = records.stream()
                                .map(e -> (Serializable) tableInfo.getPropertyValue(e, linkerField.getName()))
                                .filter(val -> val != null && !linkerNode.getLinkIdIgnore().equals(val.toString()))
                                .collect(Collectors.toSet());
                        if (CollectionUtils.isEmpty(idValues)) {
                            continue;
                        }
                        QueryWrapper<T> linkQueryWrapper = new QueryWrapper<>();
                        boolean isSameIdNamed = linkerNode.getLinkIdFieldName().equals(linkTableInfo.getKeyColumn());
                        String linkIdColumn = isSameIdNamed ? linkerNode.getLinkIdFieldName() : findColumnName(linkTableInfo, linkerNode.getLinkIdFieldName());
                        linkQueryWrapper.in(linkIdColumn, idValues);

                        // add condition of mappings
                        if (!CollectionUtils.isEmpty(mappingFields)) {
                            mappingFields.forEach((mappingFieldName, mappingNode) -> {
                                if (mappingNode.getLinkEntityType() != linkerNode.getLinkEntityType()) {
                                    return;
                                }
                                String colName = findColumnName(linkTableInfo, mappingFieldName);
                                if (colName == null) {
                                    return;
                                }
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
                                PrefectLinkNode prefectLinkNode = filterMap.get(key);
                                String linkNameColumn = findColumnName(linkTableInfo, prefectLinkNode.getLinkFieldName());
                                if (prefectLinkNode.getPrefectType() == PrefectType.IN) {
                                    linkQueryWrapper.like(linkNameColumn, prefectLinkNode.getTargetFieldValue());
                                } else {
                                    linkQueryWrapper.eq(linkNameColumn, prefectLinkNode.getTargetFieldValue());
                                }
                            }
                        }

                        // 设置查询的字段
                        List<String> queryColumns = new ArrayList<>();
                        queryColumns.add(linkTableInfo.getKeyColumn());
                        // 所有当前类型的 linkId 和 linkName 作为查询的字段
                        List<String> linkColumns = queryLinkerNodes.stream()
                                .filter(ql -> ql.getLinkEntityType() == linkerNode.getLinkEntityType())
                                .map(ql -> new String[] { ql.getLinkIdFieldName(), ql.getLinkFieldName() })
                                .flatMap(Arrays::stream)
                                .distinct()
                                .filter(f -> !queryColumns.contains(f))
                                .map(f -> findColumnName(linkTableInfo, f))
                                .collect(Collectors.toList());
                        queryColumns.addAll(linkColumns);
                        linkQueryWrapper.select(queryColumns.toArray(new String[]{}));
                        String selectTopFieldName = linkerNode.getLinkSelectTopFieldName();
                        if (selectTopFieldName != null) {
                            // 只获取顶部的一条记录
                            Integer linkSelectTop = linkerNode.getLinkSelectTop();
                            String selectTopColumnName = findColumnName(linkTableInfo, selectTopFieldName);
                            // 如果设置为-1，则为倒序
                            if (linkSelectTop == -1) {
                                linkQueryWrapper.orderBy(true, false, selectTopColumnName);
                            }
                            // one to many, find the first row
                            if (idValues.size() == 1) {
                                linkQueryWrapper.last("limit 1");
                            }
                        }
                        linkEntityList = linkMapper.selectList(linkQueryWrapper);
                        linkEntityListCacheMap.put(linkMapperClassName, linkEntityList);
                    }
                    if (CollectionUtils.isEmpty(linkEntityList)) {
                        continue;
                    }
                    if (entity2VoConverter != null && voList == null) {
                        voList = records.stream().map(entity2VoConverter).collect(Collectors.toCollection(ArrayList::new));
                    }
                    int recordSize = records.size();
                    for (int i = 0; i < recordSize; i++) {
                        T record = records.get(i);
                        Object matchIdValue = tableInfo.getPropertyValue(record, linkerField.getName());
                        List<?> matchEntityList = linkEntityList.stream()
                                .filter(linkEntity -> String.valueOf(linkTableInfo.getPropertyValue(linkEntity, linkerNode.getLinkIdFieldName())).equals(String.valueOf(matchIdValue)))
                                .collect(Collectors.toList());
                        if (CollectionUtils.isEmpty(matchEntityList)){
                            continue;
                        }
                        Field[] declaredFields = entity2VoConverter == null ? TypeReflector.getFields(tableInfo.getEntityType()) :
                                TypeReflector.getFields(voList.get(0).getClass());
                        Field targetField = Stream.of(declaredFields)
                                .filter(field -> field.getName().equals(linkerNode.getTargetFieldName()))
                                .findFirst()
                                .orElse(null);
                        if (List.class.isAssignableFrom(targetField.getType())) {
                            List<Object> targetValues = matchEntityList.stream()
                                    .map(linkEntity -> linkTableInfo.getPropertyValue(linkEntity, linkerNode.getLinkFieldName()))
                                    .collect(Collectors.toList());
                            if (entity2VoConverter == null) {
                                tableInfo.setPropertyValue(record, linkerNode.getTargetFieldName(), targetValues);
                            } else {
                                V vo = voList.get(i);
                                TypeReflector.setProps(vo, linkerNode.getTargetFieldName(), targetValues);
                            }
                            continue;
                        }
                        Object linkEntity = matchEntityList.get(0);
                        Object targetValue = linkTableInfo.getPropertyValue(linkEntity, linkerNode.getLinkFieldName());
                        if (entity2VoConverter == null) {
                            tableInfo.setPropertyValue(record, linkerNode.getTargetFieldName(), targetValue);
                            continue;
                        }
                        V vo = voList.get(i);
                        TypeReflector.setProps(vo, linkerNode.getTargetFieldName(), targetValue);
                    }
                }
            }
        }
        if (entity2VoConverter == null || voList == null) {
            uniformPage.setList((List<V>) records);
        } else {
            uniformPage.setList(voList);
        }
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
            }
            if (refMatcher.type() == RefMatcher.RefType.FOREIGN) {
                QueryWrapper foreignQueryWrapper = new QueryWrapper();
                TableInfo foreignTableInfo = TableInfoHelper.getTableInfo(refMatcher.foreignType());
                String columnName = findColumnName(foreignTableInfo, refMatcher.foreignField());
                foreignQueryWrapper.eq(columnName, id);
                BaseMapper referenceMapper = SiriusInspector.getMapper(refMatcher.foreignType());
                if (referenceMapper.selectCount(foreignQueryWrapper) > 0) {
                    return false;
                }
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
    private Object findLinkerValue(Map<String, Set<QueryLinkerNode>> linkerNodes, QueryMatcher queryMatcher, List<Field> linkerFields, Object entity, Field field, TableInfo tableInfo, Map<String, PrefectLinkNode> filterMap) {
        Object searchValue = tableInfo.getPropertyValue(entity, queryMatcher.matchDataField());
        if (searchValue == null) {
            return null;
        }
        for (Field linkerField : linkerFields) {
            // find linker field which matches `matchDataField` from QueryMatcher
            Set<QueryLinkerNode> queryLinkerNodes = linkerNodes.get(linkerField.getName());
            if (queryLinkerNodes == null) {
                continue;
            }
            QueryLinkerNode linkerNode = queryLinkerNodes.stream()
                    .filter(node -> node.getTargetFieldName().equals(queryMatcher.matchDataField())).findFirst().orElse(null);
            if (linkerNode == null) {
                continue;
            }
            Class<?> linkClass = linkerNode.getLinkEntityType();
            TableInfo linkTableInfo = TableInfoHelper.getTableInfo(linkClass);
            if (linkTableInfo == null) {
                return null;
            }

            // add this linker field name and value as condition for the link query
            PrefectLinkNode prefectLinkNode = new PrefectLinkNode();
            prefectLinkNode.setPrefectType(queryMatcher.prefect());
            prefectLinkNode.setTargetFieldValue(searchValue);
            prefectLinkNode.setTargetFieldName(linkerNode.getTargetFieldName());
            prefectLinkNode.setLinkFieldName(linkerNode.getLinkFieldName());
            filterMap.put(linkerNode.getLinkFieldName(), prefectLinkNode);
            // find link entity with linker mapper
            BaseMapper<T> linkMapper = (BaseMapper<T>) SiriusInspector.getMapper(linkClass);
            String linkNameColumn = findColumnName(linkTableInfo, linkerNode.getLinkFieldName());
            QueryWrapper<T> queryExample = new QueryWrapper<>();
            if (queryMatcher.prefect() == PrefectType.IN || queryMatcher.prefect() == PrefectType.LINK_EQ_IN) {
                if (queryMatcher.prefect() == PrefectType.IN) {
                    queryExample.like(linkNameColumn, searchValue);
                } else {
                    queryExample.eq(linkNameColumn, searchValue);
                }
                String linkIdColumn = findColumnName(linkTableInfo, linkerNode.getLinkIdFieldName());
                queryExample.select(linkTableInfo.getKeyColumn(), linkNameColumn, linkIdColumn);
                List<?> linkRecordlist = linkMapper.selectList(queryExample);
                if (CollectionUtils.isEmpty(linkRecordlist)) {
                    return genNonFoundValue(field, queryMatcher.prefect());
                }
                // collect and return link entity id list
                return linkRecordlist.stream()
                        .map(record -> linkTableInfo.getPropertyValue(record, linkerNode.getLinkIdFieldName()))
                        .collect(Collectors.toSet());
            }
            if (queryMatcher.prefect() == PrefectType.EQ) {
                queryExample.eq(linkNameColumn, searchValue);
                queryExample.select(linkTableInfo.getKeyColumn(), linkNameColumn);
                Object linkRecord = linkMapper.selectOne(queryExample);
                if (linkRecord == null) {
                    return genNonFoundValue(field, queryMatcher.prefect());
                }
                return linkTableInfo.getPropertyValue(linkRecord, linkerNode.getLinkIdFieldName());
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

    private String findColumnName(TableInfo tableInfo, Field field) {
        String columnName = findColumnName(tableInfo, field.getName());
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
        return columnName;
    }

    @EqualsAndHashCode
    @Data
    static class QueryLinkerNode {

        /**
         * match target field name
         */
        private String targetFieldName;

        /**
         * match link field name
         */
        private String linkFieldName;

        /**
         * match linker data field
         */
        private String linkIdFieldName;

        /**
         * ignore id value
         */
        private String linkIdIgnore;

        /**
         * select top row by {@link QueryLinkerNode#linkFieldName}
         */
        private String linkSelectTopFieldName;

        /**
         * select top row（0 is first, -1 is last）
         */
        private Integer linkSelectTop = 0;

        /**
         * additional field name mappings to filter link rows
         */
        private String mappings;

        /**
         * links entity class type
         */
        private Class<?> linkEntityType;

        /**
         * filter groups
         */
        private String[] group;

        static Set<QueryLinkerNode> build(Set<QueryLinker> queryLinkers) {
            return queryLinkers.stream().map(ql -> {
                QueryLinkerNode node = new QueryLinkerNode();
                node.setTargetFieldName(ql.targetNameField());

                Integer linkSelectTop = null;
                String linkSelectTopFieldName = null;
                String linkFieldName = ql.linkNameField();
                if (linkFieldName.contains("#")) {
                    int sepIndex = linkFieldName.indexOf('#');
                    linkSelectTop = Integer.parseInt(linkFieldName.substring(sepIndex + 1));
                    linkFieldName = linkFieldName.substring(0, sepIndex);
                    linkSelectTopFieldName = linkFieldName;
                }
                node.setLinkFieldName(linkFieldName);
                if (linkSelectTop != null) {
                    node.setLinkSelectTop(linkSelectTop);
                    node.setLinkSelectTopFieldName(linkSelectTopFieldName);
                }
                node.setLinkIdFieldName(ql.linkIdField());
                node.setLinkIdIgnore(ql.linkIdIgnore());
                node.setMappings(ql.mappings());
                node.setLinkEntityType(ql.type());
                node.setGroup(ql.group());
                return node;
            }).collect(Collectors.toSet());
        }

        static Set<QueryLinkerNode> build(QueryAutoLinker queryAutoLinker) {
            String[] linkerParts = queryAutoLinker.links().split(",");
            int linkerSize = linkerParts.length;
            Set<QueryLinkerNode> nodes = new HashSet<>(linkerSize);
            for (String part : linkerParts) {
                String[] linkerPart = part.split("\\s*->\\s*");
                String targetFieldName = linkerPart[0];
                String linkFieldName = linkerPart.length == 1 ? targetFieldName : linkerPart[1];
                String linkIdName = linkerPart.length == 3 ? linkerPart[2] : queryAutoLinker.linkIdField();
                String linkIdIgnore = "0";
                if (linkIdName.contains("!")) {
                    String origLinkIdName = linkIdName;
                    int sepIndex = origLinkIdName.indexOf('!');
                    linkIdName = origLinkIdName.substring(0, sepIndex);
                    linkIdIgnore = origLinkIdName.substring(sepIndex + 1);
                }
                Integer linkSelectTop = null;
                String linkSelectTopFieldName = null;
                if (linkFieldName.contains("#")) {
                    int sepIndex = linkFieldName.indexOf('#');
                    linkSelectTop = Integer.parseInt(linkFieldName.substring(sepIndex + 1));
                    linkFieldName = linkFieldName.substring(0, sepIndex);
                    linkSelectTopFieldName = linkFieldName;
                }
                QueryLinkerNode queryLinkerNode = new QueryLinkerNode();
                queryLinkerNode.setTargetFieldName(targetFieldName);
                queryLinkerNode.setLinkFieldName(linkFieldName);
                queryLinkerNode.setLinkIdFieldName(linkIdName);
                queryLinkerNode.setLinkIdIgnore(linkIdIgnore);
                if (linkSelectTop != null) {
                    queryLinkerNode.setLinkSelectTop(linkSelectTop);
                    queryLinkerNode.setLinkSelectTopFieldName(linkSelectTopFieldName);
                }
                queryLinkerNode.setMappings(queryAutoLinker.mappings());
                queryLinkerNode.setLinkEntityType(queryAutoLinker.type());
                queryLinkerNode.setGroup(queryAutoLinker.group());
                nodes.add(queryLinkerNode);
            }
            return nodes;
        }
    }

    @EqualsAndHashCode
    @Data
    static class PrefectLinkNode {
        private PrefectType prefectType;
        private Object targetFieldValue;
        private String targetFieldName;
        private String linkFieldName;
    }

    @EqualsAndHashCode
    @Data
    static class MappingNode {
        private PrefectType prefectType;
        private Object fieldValue;
        private String fieldName;
        private Class<?> linkEntityType;

        static void construct(Set<QueryLinkerNode> linkerNodes, Map<String, MappingNode> container, TableInfo tableInfo, Object target) {
            QueryLinkerNode linkerNode = linkerNodes.stream().filter(node -> StringUtils.isNotEmpty(node.getMappings())).findFirst().orElse(null);
            if (linkerNode != null) {
                String[] mappings = linkerNode.getMappings().split(",");
                Arrays.stream(mappings)
                        .map(StringUtils::strip)
                        .map(m -> m.split("\\s*->\\s*"))
                        .forEach(m -> {
                            String targetFieldName = m[0];
                            String fieldName = m.length == 1 ? targetFieldName : m[1];
                            Object fieldValue = null;
                            if (m.length == 1) {
                                if (fieldName.contains("=")) {
                                    int sepIndex = fieldName.indexOf('=');
                                    // 判断字符串是数字
                                    fieldValue = fieldName.substring(sepIndex + 1);
                                    String fieldValueLiteral = (String) fieldValue;
                                    if (StringUtils.isNumeric(fieldValueLiteral)) {
                                        fieldValue = Integer.parseInt(fieldValueLiteral);
                                    }
                                    fieldName = fieldName.substring(0, sepIndex);
                                }
                            } else {
                                fieldValue = tableInfo.getPropertyValue(target, targetFieldName);
                                if (fieldValue == null) {
                                    fieldValue = ReflectUtil.invokeFieldPath(target, targetFieldName);
                                }
                            }

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
                            mappingNode.setLinkEntityType(linkerNode.getLinkEntityType());
                            container.put(fieldName, mappingNode);
                        });
            }
        }
    }
}
