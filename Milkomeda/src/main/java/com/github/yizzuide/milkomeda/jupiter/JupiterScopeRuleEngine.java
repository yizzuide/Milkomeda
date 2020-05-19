package com.github.yizzuide.milkomeda.jupiter;

import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.universe.exception.NotImplementException;
import com.github.yizzuide.milkomeda.universe.parser.url.URLPlaceholderParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * JupiterScopeRuleEngine
 * 具有请求领域上下文的规则引擎
 *
 * @author yizzuide
 * @since 3.5.0
 * Create at 2020/05/19 14:39
 */
@Slf4j
public class JupiterScopeRuleEngine implements JupiterRuleEngine {

    private final URLPlaceholderParser urlPlaceholderParser = new URLPlaceholderParser();

    private List<JupiterRuleItem> ruleItemList;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @SuppressWarnings("rawtypes")
    @Override
    public boolean run() {
        try {
            for (JupiterRuleItem ruleItem : ruleItemList) {
                JupiterExpressCompiler compiler = JupiterCompilerPool.get(ruleItem.getSyntax());
                if (compiler == null) {
                    throw new NotImplementException("Can't find type implement with JupiterCompilerType." + ruleItem.getSyntax());
                }
                Boolean pass;
                if (ruleItem.getDomain() == null) {
                    pass = compiler.compile(ruleItem.getMatch(), null, Boolean.TYPE);
                    assert pass != null;
                    if (!pass) {
                        return false;
                    }
                    continue;
                }
                String filter = ruleItem.getFilter();
                // 解析条件表达式
                if (filter != null && filter.contains("{$")) {
                    Map<String, List<String>> placeHolders = urlPlaceholderParser.grabPlaceHolders(filter);
                    filter = urlPlaceholderParser.parse(filter, WebContext.getRequest(), null, placeHolders);
                }

                // 解析SQL
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
                String sql = sqlBuilder.toString();
                if (ruleItem.isMulti()) {
                    List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
                    pass = compiler.compile(ruleItem.getMatch(), rows, Boolean.TYPE);
                } else {
                    Map row = jdbcTemplate.queryForMap(sql);
                    pass = compiler.compile(ruleItem.getMatch(), row, Boolean.TYPE);
                }
                assert pass != null;
                if (!pass) {
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("Jupiter rule engine execute error with msg: {}", e.getMessage(), e);
        }
        return true;
    }

    public void configRuleItems(List<JupiterRuleItem> ruleItemList) {
        this.ruleItemList = ruleItemList;
        int incr = 0;
        for (JupiterRuleItem ruleItem : ruleItemList) {
            if (ruleItem.getId() == null) {
                ruleItem.setId(++incr);
            }
        }
    }
}
