package com.github.yizzuide.milkomeda.hydrogen;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * HydrogenProperties
 *
 * @author yizzuide
 * @since 2.8.0
 * Create at 2020/03/25 17:47
 */
@Data
@ConfigurationProperties("milkomeda.hydrogen")
public class HydrogenProperties {
    /**
     * 切面事务
     */
    private HydrogenTransaction transaction;

    @Data
    static class HydrogenTransaction {
        /**
         * 开启AOP事务
         */
        private boolean enable = true;
        /**
         * 切点表达式
         */
        private String pointcutExpression = "execution(* com..service.*.*(..))";
        /**
         * 事务超时回滚
         */
        private int rollbackWhenTimeout = 5000;
        /**
         * 指定异常类回滚
         */
        private List<Class<? extends Exception>> rollbackWhenException = Collections.singletonList(Exception.class);
        /**
         * 只读事务方法前辍
         */
        private List<String> readOnlyPrefix = Arrays.asList("get*", "query*", "find*", "select*", "list*", "count*", "is*");
        /**
         * 追加只读事务方法前辍
         */
        private List<String> readOnlyAppendPrefix;
    }

}
