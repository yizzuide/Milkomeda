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
public class JupiterElCompiler implements JupiterExpressCompiler {

    private final ExpressionParser parser = new SpelExpressionParser();

    @Override
    public <T> T compile(String express, Object context, Class<T> resultType) {
        Expression expression = parser.parseExpression(express);
        if (context == null) {
            return expression.getValue(resultType);
        }
        return expression.getValue(context, resultType);
    }
}
