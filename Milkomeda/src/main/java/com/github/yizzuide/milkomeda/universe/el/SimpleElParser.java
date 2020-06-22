package com.github.yizzuide.milkomeda.universe.el;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * SimpleElParser
 *
 * @author yizzuide
 * @since 3.8.0
 * Create at 2020/06/16 10:25
 */
public class SimpleElParser {

    private static final ExpressionParser EL_PARSER = new SpelExpressionParser();

    public static <T> T parse(String expression, Object root, Class<T> resultType) {
        Expression expressionWrapper = EL_PARSER.parseExpression(expression);
        if (root == null) {
            return expressionWrapper.getValue(resultType);
        }
        return expressionWrapper.getValue(root, resultType);
    }
}
