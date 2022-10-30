package com.github.yizzuide.milkomeda.sirius;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformPage;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformQueryPageData;
import com.github.yizzuide.milkomeda.util.DataTypeConvertUtil;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
        Field[] fields = tClass.getDeclaredFields();
        boolean postOrderByASC = true;
        boolean needPostOrderBy = false;
        Field orderField = null;
        for (Field field : fields) {
            QueryMatcher queryMatcher = field.getDeclaredAnnotation(QueryMatcher.class);
            if (queryMatcher == null) {
                continue;
            }
            TableField tableField = field.getDeclaredAnnotation(TableField.class);
            String columnName;
            if (tableField != null) {
                columnName = tableField.value();
            } else {
                columnName = DataTypeConvertUtil.humpToLine(field.getName());
            }

            ReflectionUtils.makeAccessible(field);
            Object fieldValue = ReflectionUtils.getField(field, target);
            boolean fieldNonNull = Objects.nonNull(target) && Objects.nonNull(fieldValue);
            if (queryMatcher.prefect() == PrefectType.EQ) {
                queryWrapper.eq(fieldNonNull, columnName, fieldValue);
            } else if (queryMatcher.prefect() == PrefectType.LIKE) {
                queryWrapper.like(fieldNonNull, columnName, fieldValue);
            } else if (queryMatcher.prefect() == PrefectType.PageDate) {
                queryWrapper.ge(Objects.nonNull(queryPageData.getStartDate()), columnName, queryPageData.getStartDate());
                queryWrapper.le(Objects.nonNull(queryPageData.getEndDate()), columnName, queryPageData.getEndDate());
            } else if (queryMatcher.prefect() == PrefectType.OrderByPre) {
                queryWrapper.orderBy(true, queryMatcher.forward(), columnName);
            } else if (queryMatcher.prefect() == PrefectType.OrderByPost) {
                needPostOrderBy = true;
                postOrderByASC = queryMatcher.forward();
                orderField = field;
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
                // reflect from field
                Field finalOrderField = orderField;
                records.sort(Comparator.comparingInt(t -> (Integer) ReflectionUtils.getField(finalOrderField, t)));
            }
            // need desc?
            if (!postOrderByASC) {
                Collections.reverse(records);
            }
        }
        uniformPage.setList(records);
        return uniformPage;
    }
}
