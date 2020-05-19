package com.github.yizzuide.milkomeda.jupiter;

import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.universe.parser.url.URLPlaceholderParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JupiterRuleELEngine
 *
 * @author yizzuide
 * @since 3.5.0
 * Create at 2020/05/19 14:39
 */
@Slf4j
public class JupiterRuleELEngine implements JupiterRuleEngine {

    private final ExpressionParser parser = new SpelExpressionParser();

    private final URLPlaceholderParser urlPlaceholderParser = new URLPlaceholderParser();

    private final List<JupiterRuleItem> ruleItemList = new ArrayList<>();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        refreshRules();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean inspect() {
        try {
            for (JupiterRuleItem ruleItem : ruleItemList) {
                Expression exp = parser.parseExpression(ruleItem.getMatch());
                Boolean pass;
                if (ruleItem.getDomain() == null) {
                    pass = exp.getValue(Boolean.TYPE);
                    assert pass != null;
                    if (!pass) {
                        return false;
                    }
                    continue;
                }
                StringBuilder sqlBuilder = new StringBuilder("select " + ruleItem.getFields() + " from " + ruleItem.getDomain());
                String filter = ruleItem.getFilter();
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
                    pass = exp.getValue(rows, Boolean.TYPE);
                } else {
                    Map row = jdbcTemplate.queryForMap(sql);
                    pass = exp.getValue(row, Boolean.TYPE);
                }
                assert pass != null;
                if (!pass) {
                    return false;
                }
            }
        } catch (Exception e) {
            log.info("Jupiter rule engine execute error with msg: {}", e.getMessage(), e);
        }
        return true;
    }

    /**
     * 添加规则配置
     * @param ruleItem  JupiterRuleItem
     */
    public void addRuleItem(JupiterRuleItem ruleItem) {
        ruleItemList.add(ruleItem);
    }

    /**
     * 刷新规则配置
     */
    public void refreshRules() {
        // 预解析条件表达式
        for (JupiterRuleItem ruleItem : ruleItemList) {
            Map<String, List<String>> placeHolders = urlPlaceholderParser.grabPlaceHolders(ruleItem.getFilter());
            ruleItem.setFilter(urlPlaceholderParser.parse(ruleItem.getFilter(), WebContext.getRequest(), null, placeHolders));
        }
    }
}
