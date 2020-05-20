package com.github.yizzuide.milkomeda.jupiter;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * JupiterElCompiler
 * EL表达式解析器
 *
 * @author yizzuide
 * @since 3.5.0
 * Create at 2020/05/19 21:45
 */
public class JupiterElCompiler implements JupiterExpressionCompiler {

    private final ExpressionParser EL_PARSER = new SpelExpressionParser();

    @Override
    public <T> T compile(String expression, Object root, Class<T> resultType) {
        Expression expressionWrapper = EL_PARSER.parseExpression(expression);
        if (root == null) {
            return expressionWrapper.getValue(resultType);
        }
        return expressionWrapper.getValue(root, resultType);
    }
}
