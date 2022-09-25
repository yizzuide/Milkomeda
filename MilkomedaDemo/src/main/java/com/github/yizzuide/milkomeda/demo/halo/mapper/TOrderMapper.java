package com.github.yizzuide.milkomeda.demo.halo.mapper;

import com.github.yizzuide.milkomeda.demo.halo.domain.TOrder;
import com.github.yizzuide.milkomeda.sundial.ShardingType;
import com.github.yizzuide.milkomeda.sundial.Sundial;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 订单表(TOrder)表数据库访问层
 *
 * @author makejava
 * @since 2020-01-30 19:32:33
 */
public interface TOrderMapper {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    TOrder queryById(Long id);

    TOrder queryByIdAndTime(@Param("id") Long id, @Param("date") Date date);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<TOrder> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param tOrder 实例对象
     * @return 对象列表
     */
    List<TOrder> queryAll(TOrder tOrder);

    /**
     * 新增数据
     *
     * @param tOrder 实例对象
     * @return 影响行数
     */
    // ShardingType.TABLE：仅分表, partExp：分表表达式（table为表名，p为参数，fn为函数调用）
    @Sundial(shardingType = ShardingType.TABLE, partExp = "fn.format(table + '_%03d', fn.fnv(p.orderNo, 2, 4))")
    int insert(TOrder tOrder);

    /**
     * 修改数据
     *
     * @param tOrder 实例对象
     * @return 影响行数
     */
    int update(TOrder tOrder);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

}