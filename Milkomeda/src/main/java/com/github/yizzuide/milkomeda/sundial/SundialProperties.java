package com.github.yizzuide.milkomeda.sundial;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Driver;
import java.util.HashMap;
import java.util.Map;


/**
 * 自定义数据源属性配置
 * @author jsq 786063250@qq.com
 * @since 3.4.0
 * Create at 2020/5/8
 */
@Data
@Component
@ConfigurationProperties(prefix = "milkomeda.sundial")
public class SundialProperties {

    /**
     * 数据源模版前缀
     */
    public String configPrefix = "spring.datasource";

    /**
     * 数据源类型
     */
    private Class<? extends DataSource> datasourceType = HikariDataSource.class;

    /**
     * 数据源实例
     */
    private Map<String,Datasource> instances = new HashMap<>();

    /**
     * 数据源配置
     */
    @Data
    public static class Datasource {

        /**
         * 数据库链接地址
         */
        private String url;

        /**
         * 数据库驱动类名
         */
        private Class<? extends Driver> driverClassName;

        /**
         * 账号
         */
        private String username;

        /**
         * 密码
         */
        private String password;
    }


}
