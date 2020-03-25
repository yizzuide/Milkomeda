package com.github.yizzuide.milkomeda.hydrogen;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

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
    private final HydrogenTransaction transaction = new HydrogenTransaction();

    /**
     * 统一异常处理
     */
    private final ResponseFrontException responseFrontException = new ResponseFrontException();

    @Data
    public static class HydrogenTransaction {
        /**
         * 开启AOP事务
         */
        private boolean enable = true;
        /**
         * 切点表达式
         */
        private String pointcutExpression = "execution(* com..service.*.*(..))";
        /**
         * 事务超时回滚（单位：s。-1：不设置超时回滚）
         */
        @DurationUnit(ChronoUnit.SECONDS)
        private Duration rollbackWhenTimeout = Duration.ofSeconds(-1);
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

    @Data
    public static class ResponseFrontException {
        /**
         * 启用统一异常处理
         */
        private boolean enable = false;
        /**
         * 响应数据
         */
        private Map<String, Object> body = new HashMap<>();
    }

}
