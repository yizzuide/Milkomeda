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
        Map<String, Set<QueryLinkerNode>> linkerNodes = new HashMap<>();
        Map<String, MappingNode> mappingFields = new HashMap<>();
        // 需要在linker关联结果过滤条件
        Map<String, PrefectLinkNode> filterMap = new HashMap<>();
        for (Field field : fields) {
            // convert QueryAutoLinker to QueryLinkerNode
            QueryAutoLinker queryAutoLinker = AnnotationUtils.findAnnotation(field, QueryAutoLinker.class);
            if (queryAutoLinker != null) {
                Set<QueryLinkerNode> nodes = QueryLinkerNode.build(queryAutoLinker);
                linkerNodes.put(field.getName(), nodes);
                linkerFields.add(field);
            }

            // convert QueryLinker to QueryLinkerNode
            Set<QueryLinker> queryLinkers = AnnotatedElementUtils.getMergedRepeatableAnnotations(field, QueryLinker.class, QueryLinkers.class);
            if (!CollectionUtils.isEmpty(queryLinkers)) {
                Set<QueryLinkerNode> nodes = QueryLinkerNode.build(queryLinkers);
                linkerNodes.put(field.getName(), nodes);
                linkerFields.add(field);
            }

            // collect mappings
            if (!linkerNodes.isEmpty() && linkerNodes.containsKey(field.getName())) {
                MappingNode.construct(linkerNodes.get(field.getName()), mappingFields, tableInfo, target);
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
                        fieldValue = findLinkerValue(linkerNodes, queryMatcher, linkerFields, queryPageData.getEntity(), field, tableInfo, filterMap);
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
                        valueObjects = (Collection<Object>) findLinkerValue(linkerNodes, queryMatcher, linkerFields, queryPageData.getEntity(), field, tableInfo, filterMap);
                        condition = valueObjects != null;
                    }
                    queryWrapper.in(condition, columnName, valueObjects);
                } else if (queryMatcher.prefect() == PrefectType.PageDate) {
                    queryWrapper.ge(Objects.nonNull(queryPageData.getStartDate()), columnName, queryPageData.getStartDate());
                    queryWrapper.le(Objects.nonNull(queryPageData.getEndDate()), columnName, queryPageData.getEndDate());
                } else if (queryMatcher.prefect() == PrefectType.PageUnixTime) {
                    queryWrapper.ge(Objects.nonNull(queryPageData.getStartDate()), columnName, queryPageData.getStartUnixTime());
                    queryWrapper.le(Objects.nonNull(queryPageData.getEndDate()), columnName, queryPageData.getEndUnixTime());
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
            String sqlSelect = queryWrapper.getSqlSelect();
            queryWrapper.select("*");
            Long totalSize = this.baseMapper.selectCount(queryWrapper);
            if (totalSize > 10000) {
                throw new UnsupportedOperationException("Record row over more then 10000");
            }
            queryWrapper.select(sqlSelect);
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
                Set<QueryLinkerNode> queryLinkerNodes = linkerNodes.get(linkerField.getName());
                for (QueryLinkerNode linkerNode : queryLinkerNodes) {
                    if (!Arrays.asList(linkerNode.getGroup()).contains(group)) {
                        continue;
                    }
                    BaseMapper linkMapper = SiriusInspector.getMapper(linkerNode.getLinkEntityType());
                    List<?> linkEntityList;
                    String linkMapperClassName = linkMapper.getClass().getName();
                    // get link entity table info
                    TableInfo linkTableInfo = TableInfoHelper.getTableInfo(linkerNode.getLinkEntityType());
                    if (linkEntityListCacheMap.containsKey(linkMapperClassName)) {
                        linkEntityList = linkEntityListCacheMap.get(linkMapperClassName);
                    } else {
                        Set<Serializable> idValues = records.stream()
                                .map(e -> (Serializable) tableInfo.getPropertyValue(e, linkerField.getName()))
                                .filter(e -> !linkerNode.getLinkIdIgnore().equals(e.toString()))
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
                        // 所有当前类型的linkId和LinkName
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
                        linkEntityList = linkMapper.selectList(linkQueryWrapper);
                        linkEntityListCacheMap.put(linkMapperClassName, linkEntityList);
                    }
                    if (!CollectionUtils.isEmpty(linkEntityList)) {
                        for (T record : records) {
                            for (Object le : linkEntityList) {
                                Object linkId = linkTableInfo.getPropertyValue(le, linkerNode.getLinkIdFieldName());
                                Object nameValue = linkTableInfo.getPropertyValue(le, linkerNode.getLinkFieldName());
                                Object matchIdValue = tableInfo.getPropertyValue(record, linkerField.getName());
                                if (String.valueOf(matchIdValue).equals(String.valueOf(linkId))) {
                                    tableInfo.setPropertyValue(record, linkerNode.getTargetFieldName(), nameValue);
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

    @Data
    static class QueryLinkerNode {
        private String targetFieldName;
        private String linkFieldName;
        private String linkIdFieldName;
        private String linkIdIgnore;
        private String mappings;
        private Class<?> linkEntityType;
        private String[] group;

        static Set<QueryLinkerNode> build(Set<QueryLinker> queryLinkers) {
            return queryLinkers.stream().map(ql -> {
                QueryLinkerNode node = new QueryLinkerNode();
                node.setTargetFieldName(ql.targetNameField());
                node.setLinkFieldName(ql.linkNameField());
                node.setLinkIdFieldName(ql.linkIdField());
                node.setLinkIdIgnore(ql.linkIdIgnore());
                node.setMappings(ql.mappings());
                node.setLinkEntityType(ql.linkEntityType());
                node.setGroup(ql.group());
                return node;
            }).collect(Collectors.toSet());
        }

        static Set<QueryLinkerNode> build(QueryAutoLinker queryAutoLinker) {
            String[] linkerParts = queryAutoLinker.links().split(",");
            int linkerSize = linkerParts.length;
            Set<QueryLinkerNode> nodes = new HashSet<>(linkerSize);
            for (int i = 0; i < linkerSize; i++) {
                String[] linkerPart = linkerParts[i].split("\\s*->\\s*");
                String targetFieldName = linkerPart[0];
                String linkFieldName = linkerPart.length == 1? targetFieldName : linkerPart[1];
                String linkIdName = linkerPart.length == 3 ? linkerPart[2]: queryAutoLinker.linkIdField();
                String linkIdIgnore = "0";
                if (linkIdName.contains("!")) {
                    String origLinkIdName = linkIdName;
                    int sepIndex = origLinkIdName.indexOf('!');
                    linkIdName = origLinkIdName.substring(0, sepIndex);
                    linkIdIgnore = origLinkIdName.substring(sepIndex + 1);
                }
                String[] matchGroup = new String[] { "default" };
                if (queryAutoLinker.groups() != null && queryAutoLinker.groups().length != 0) {
                    String groupItem = queryAutoLinker.groups()[i];
                    matchGroup = groupItem.split(",");
                }
                QueryLinkerNode queryLinkerNode = new QueryLinkerNode();
                queryLinkerNode.setTargetFieldName(targetFieldName);
                queryLinkerNode.setLinkFieldName(linkFieldName);
                queryLinkerNode.setLinkIdFieldName(linkIdName);
                queryLinkerNode.setLinkIdIgnore(linkIdIgnore);
                queryLinkerNode.setMappings(queryAutoLinker.mappings());
                queryLinkerNode.setLinkEntityType(queryAutoLinker.type());
                queryLinkerNode.setGroup(matchGroup);
                nodes.add(queryLinkerNode);
            }
            return nodes;
        }
    }

    @Data
    static class PrefectLinkNode {
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

        static void construct(Set<QueryLinkerNode> nodes, Map<String, MappingNode> container, TableInfo tableInfo, Object target) {
            QueryLinkerNode node = nodes.stream().filter(ql -> StringUtils.isNotEmpty(ql.getMappings())).findFirst().orElse(null);
            if (node != null) {
                String[] mappings = node.getMappings().split(",");
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
                            container.put(fieldName, mappingNode);
                        });
            }
        }
    }
}
