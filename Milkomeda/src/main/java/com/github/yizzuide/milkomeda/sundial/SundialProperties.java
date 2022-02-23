/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.sundial;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Driver;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 自定义数据源属性配置
 * @author jsq 786063250@qq.com
 * @since 3.4.0
 * @version 3.8.0
 * Create at 2020/5/8
 */
@Data
@Component
@ConfigurationProperties(prefix = SundialProperties.PREFIX)
public class SundialProperties {
    // 当前配置前缀
    static final String PREFIX = "milkomeda.sundial";

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
    private Map<String, Datasource> instances = new HashMap<>();

    /**
     * 数据切换策略
     */
    private List<Strategy> strategy;

    /**
     * 是否开启分库分表
     */
    private boolean enableSharding = false;

    /**
     * 分库分表
     */
    private Sharding sharding = new Sharding();

    /**
     * 分库分表
     */
    @Data
    public static class Sharding {
        /**
         *  只包含名称的作为第一个序号（用于在不需要修改名称下扩容）
         */
        private boolean originalNameAsIndexZero;

        /**
         * 索引连接符
         */
        private String indexSeparator = "_";

        /**
         * 连接节点
         */
        private Map<String, DataNode> nodes;
    }

    /**
     * 连接节点，一个节点包括一主多从数据源
     */
    @Data
    public static class DataNode {
        /**
         * 数据库名（如果数据源key已指定数据库，则不需要设置）
         */
        private String schema;

        /**
         * 主数据源key
         */
        private String leader;

        /**
         * 从数据源key
         */
        private List<String> follows;
    }

    /**
     * 主从切换策略
     */
    @Data
    public static class Strategy {
        public static final String KEY_NAME = "keyName";
        /**
         * 数据源key名
         */
        private String keyName;

        /**
         * 切点表达式，如应用给Mapper的query方法：execution(* com..mapper.*.query*(..))
         */
        private String pointcutExpression;
    }

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
