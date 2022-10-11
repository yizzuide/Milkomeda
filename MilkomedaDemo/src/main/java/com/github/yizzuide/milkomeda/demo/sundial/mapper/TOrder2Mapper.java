package com.github.yizzuide.milkomeda.demo.sundial.mapper;

import com.github.yizzuide.milkomeda.demo.halo.domain.TOrder;
import com.github.yizzuide.milkomeda.sundial.ShardingType;
import com.github.yizzuide.milkomeda.sundial.Sundial;

import java.util.List;

/**
 * TOrder2Mapper
 *
 * @author yizzuide
 * <br>
 * Create at 2020/06/19 16:09
 */
public interface TOrder2Mapper {

    // ShardingType.TABLE：仅分表
    // partExp：分表表达式
    //  - table: 内部会解析表名
    //  - p为参数:
    //      - 当只一个参数，p为这个对象的引用
    //      - 当为多个参数时，p为一个Map，通过p['key']来获到值
    //  - fn为函数调用（相关函数在ShardingFunction类，可注入bean名为ShardingFunction的ShardingFunction子类型来扩展函数库）
    //      - 取模函数：mod
    //      - 自定义序列号截取：seq
    //      - 定制序列号截取: id（需要通过ShardingId类生成）
    //      - 一致性Hash函数：ketama、fnv、murmur
    //      - 自定义Hash函数：hash
    //          1. 调用前注册：CachedConsistentHashRing.getInstance().register("hashName", HashFunc实现);
    //          2. 表达式调用：fn.hash("hashName", key, nodeCount, replicas)
//    @Sundial(shardingType = ShardingType.TABLE, partExp = "fn.format(table + '_%03d', fn.ketama(p.orderNo, 2, 4))")
    // ShardingType.SCHEMA：仅分库
    @Sundial(shardingType = ShardingType.SCHEMA, nodeExp = "fn.format('node_%03d', fn.ketama(p.orderNo, 2, 4))")
    int insert(TOrder tOrder);

//    @Sundial(shardingType = ShardingType.TABLE, partExp = "fn.format(table + '_%03d', fn.ketama(p, 2, 4))")
    @Sundial(shardingType = ShardingType.SCHEMA, nodeExp = "fn.format('node_%03d', fn.ketama(p, 2, 4))")
    TOrder findByOrderNo(Long orderNo);

    List<TOrder> queryAll(TOrder tOrder);

}
