package com.github.yizzuide.milkomeda.hydrogen.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * HydrogenProperties
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/03/25 17:47
 */
@Data
@ConfigurationProperties("milkomeda.hydrogen")
public class HydrogenProperties {
    /**
     * 切面事务
     */
    @NestedConfigurationProperty
    private final Transaction transaction = new Transaction();

    /**
     * 校验器
     */
    @NestedConfigurationProperty
    private Validator validator = new Validator();

    /**
     * 国际化
     */
    @NestedConfigurationProperty
    private I18n i18n = new I18n();

    /**
     * 动态拦截器
     */
    @NestedConfigurationProperty
    private Interceptor interceptor = new Interceptor();

    /**
     * 动态过滤器
     */
    @NestedConfigurationProperty
    private Filter filter = new Filter();

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
         * 启用拦截器模块
         */
        private boolean enable = false;
        /**
         * 拦截器列表
         */
        private List<Interceptors> interceptors;
    }

    @Data
    public static class Interceptors {
        /**
         * 拦截器类
         */
        private Class<?> clazz;

        /**
         * 包括拦截的URL
         */
        private List<String> includeURLs;

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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Interceptors that = (Interceptors) o;
            return clazz.equals(that.clazz);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clazz);
        }
    }

    @Data
    public static class Filter {
        /**
         * 启用过滤模块
         */
        private boolean enable = false;

        /**
         * 过滤器列表
         */
        private List<Filters> filters;
    }

    @Data
    public static class Filters {
        /**
         * 过滤器名
         */
        private String name;
        /**
         * 过滤器类
         */
        private Class<? extends javax.servlet.Filter> clazz;

        /**
         * 匹配的URL
         */
        private List<String> urlPatterns = Collections.singletonList("/*");

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Filters filters = (Filters) o;
            return name.equals(filters.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }

}
