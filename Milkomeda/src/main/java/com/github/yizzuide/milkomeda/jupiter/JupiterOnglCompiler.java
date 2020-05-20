package com.github.yizzuide.milkomeda.jupiter;

import com.github.yizzuide.milkomeda.universe.ognl.OgnlClassResolver;
import com.github.yizzuide.milkomeda.universe.ognl.OgnlMemberAccess;
import lombok.extern.slf4j.Slf4j;
import ognl.ClassResolver;
import ognl.MemberAccess;
import ognl.Ognl;
import ognl.OgnlException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JupiterOnglCompiler
 *
 * @author yizzuide
 * @since 3.5.0
 * Create at 2020/05/20 11:39
 */
@Slf4j
public class JupiterOnglCompiler implements JupiterExpressionCompiler {
    private final MemberAccess MEMBER_ACCESS = new OgnlMemberAccess(false);
    private final ClassResolver CLASS_RESOLVER = new OgnlClassResolver();
    private final Map<String, Object> expressionCache = new ConcurrentHashMap<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T> T compile(String expression, Object root, Class<T> resultType) {
        try {
            Map ognlContext = Ognl.createDefaultContext(root, MEMBER_ACCESS, CLASS_RESOLVER, null);
            return (T) Ognl.getValue(parseExpression(expression), ognlContext, root, resultType);
        } catch (OgnlException e) {
            log.error("Jupiter ongl compiler error with msg: {}", e.getMessage(), e);
        }
        return null;
    }

    private Object parseExpression(String expression) throws OgnlException {
        Object node = expressionCache.get(expression);
        if (node == null) {
            node = Ognl.parseExpression(expression);
            expressionCache.put(expression, node);
        }
        return node;
    }
}
