package com.github.yizzuide.milkomeda.jupiter;

/**
 * JupiterSqlBuilder
 *
 * @author yizzuide
 * @since 3.5.0
 * Create at 2020/05/20 15:05
 */
public class JupiterSqlBuilder {
    /**
     * 构建查询SQL
     * @param ruleItem  规则项
     * @param filter    过滤条件
     * @return  sql
     */
    public static String build(JupiterRuleItem ruleItem, String filter) {
        StringBuilder sqlBuilder = new StringBuilder("select " + ruleItem.getFields() + " from " + ruleItem.getDomain());
        if (filter != null) {
            sqlBuilder.append(" where ");
            String[] condList = filter.split("&");
            for (int i = 0; i < condList.length; i++) {
                String cond = condList[i];
                String[] kv = cond.split("=");
                sqlBuilder.append(kv[0]).append("=").append(kv[1]);
                if (i + 1 != condList.length) {
                    sqlBuilder.append(" and ");
                }
            }
        }
        if (!ruleItem.isMulti()) {
            sqlBuilder.append(" limit 1");
        }
        return sqlBuilder.toString();
    }
}
