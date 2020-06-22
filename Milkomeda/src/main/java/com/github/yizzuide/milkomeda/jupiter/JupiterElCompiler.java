package com.github.yizzuide.milkomeda.jupiter;

import com.github.yizzuide.milkomeda.universe.el.SimpleElParser;

/**
 * JupiterElCompiler
 * EL表达式解析器
 *
 * @author yizzuide
 * @since 3.5.0
 * @version 3.8.0
 * Create at 2020/05/19 21:45
 */
public class JupiterElCompiler implements JupiterExpressionCompiler {

    @Override
    public <T> T compile(String expression, Object root, Class<T> resultType) {
        return SimpleElParser.parse(expression, root, resultType);
    }
}
