package com.github.yizzuide.milkomeda.hydrogen.transaction;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * TransactionProperties
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/06 00:51
 */
@Data
@ConfigurationProperties("milkomeda.hydrogen.transaction")
public class TransactionProperties {
    /**
     * 启用AOP事务
     */
    private boolean enable = false;
    /**
     * 切点表达式
     */
    private String pointcutExpression = "execution(* com..service.*.*(..))";
    /**
     * 事务超时回滚（默认单位：s。-1：不设置超时回滚）
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
