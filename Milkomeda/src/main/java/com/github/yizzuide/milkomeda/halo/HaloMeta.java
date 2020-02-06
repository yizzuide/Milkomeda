package com.github.yizzuide.milkomeda.halo;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.ibatis.mapping.SqlCommandType;

/**
 * HaloMeta
 * 元数据
 *
 * @author yizzuide
 * @since 2.5.1
 * Create at 2020/02/06 14:38
 */
@Data
@AllArgsConstructor
public class HaloMeta {
    /**
     * 命令类型
     */
    private SqlCommandType sqlCommandType;
    /**
     * 表名
     */
    private String tableName;
    /**
     * 参数
     */
    private Object param;
    /**
     * 返回值
     */
    private Object result;
}
