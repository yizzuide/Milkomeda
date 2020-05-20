package com.github.yizzuide.milkomeda.universe.extend.jdbc;

import com.github.yizzuide.milkomeda.util.DataTypeConvertUtil;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

/**
 * CamelCaseColumnMapRowMapper
 * 扩展JdbcTemplate行映射字段名下划线转驼峰
 *
 * @author yizzuide
 * @since 3.5.0
 * Create at 2020/05/20 15:34
 */
public class CamelCaseColumnMapRowMapper extends ColumnMapRowMapper {
    @Override
    public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        Map<String, Object> mapOfColumnValues = createColumnMap(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            String column = JdbcUtils.lookupColumnName(rsmd, i);
            mapOfColumnValues.putIfAbsent(DataTypeConvertUtil.toCamelCase(getColumnKey(column)), getColumnValue(rs, i));
        }
        return mapOfColumnValues;
    }
}
