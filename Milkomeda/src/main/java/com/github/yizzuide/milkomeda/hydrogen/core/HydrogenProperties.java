package com.github.yizzuide.milkomeda.hydrogen.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.web.servlet.HandlerInterceptor;

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
    @NestedConfigurationProperty
    private final Transaction transaction = new HydrogenProperties.Transaction();

    /**
     * 统一异常处理
     */
    @NestedConfigurationProperty
    private final Uniform uniform = new HydrogenProperties.Uniform();

    /**
     * 校验器
     */
    @NestedConfigurationProperty
    private Validator validator = new HydrogenProperties.Validator();

    /**
     * 国际化
     */
    @NestedConfigurationProperty
    private I18n i18n = new HydrogenProperties.I18n();

    /**
     * 动态拦截器
     */
    private List<Interceptor> interceptors;

    @Data
    public static class Transaction {
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

    @Data
    public static class Uniform {
        /**
         * 启用统一异常处理
         */
        private boolean enable = false;
        /**
         * 响应数据
         */
        private Map<String, Object> body = new HashMap<>();
    }

    @Data
    public static class Validator {
        /**
         * 启用验证器
         */
        private boolean enable = false;
        /**
         * 手机号正则表达式
         */
        private String regexPhone = "^((13[0-9])|(14[579])|(15([0-3]|[5-9]))|(166)|(17[0135678])|(18[0-9])|(19[8|9]))\\d{8}$";
    }

    @Data
    public static class I18n {
        /**
         * 启用国际化
         */
        private boolean enable = false;

        /**
         * 请求语言设置参数名，参数值如：zh_CN （language_country）
         */
        private String query = "lang";

        /*
         * 记录语言选择到会话（API项目设置为false，后端管理项目保存默认）
         */
        // private boolean useSessionQuery = true;

        /*
         * 设置请求语言设置到会话的key（如无特殊情况，不需要修改）
         */
        // private String querySessionName = "hydrogen_i18n_language_session";
    }

    @Data
    public static class Interceptor {
        /**
         * 拦截器类
         */
        private Class<HandlerInterceptor> clazz;

        /**
         * 包括拦截的URL
         */
        private List<String> includeURLs = Collections.singletonList("/**");

        /**
         * 排除拦截的URL
         */
        private List<String> excludeURLs;

        /**
         * 拦截器执行顺序
         */
        private int order = 0;

        /**
         * 属性
         */
        private Map<String, Object> props = new HashMap<>();
    }

}
