package com.github.yizzuide.milkomeda.sundial;

import com.github.yizzuide.milkomeda.util.DataTypeConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertyNameAliases;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Map;


/**
 * 动态创建数据源工厂
 * @author jsq 786063250@qq.com
 * @since 3.4.0
 * Create at 2020/5/8
 */
@Slf4j
public class DataSourceFactory implements EnvironmentAware {

    @Resource
    private SundialProperties sundialProperties;

    public static String DEFAULT_DATASOURCE_PREFIX = "spring.datasource";

    private static final ConfigurationPropertyNameAliases ALIASES = new ConfigurationPropertyNameAliases();

    /**
     * 配置绑定
     */
    private Binder binder;

    @PostConstruct
    public void init() {
        DEFAULT_DATASOURCE_PREFIX = sundialProperties.getConfigPrefix();
        ALIASES.addAliases("url", "jdbc-url");
        ALIASES.addAliases("username", "user");
    }

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        binder = Binder.get(environment);
    }

    /**
     * 创建用户配置的数据源
     * @return DataSource
     * @throws Exception    创建异常
     */
    public DataSource createDataSource() throws Exception {
        return createDataSource(null);
    }

    /**
     * 创建数据源实例
     * @return DataSource
     * @throws Exception    创建异常
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public DataSource createDataSource(SundialProperties.Datasource dataSourceConf) throws Exception {
        Map dataSourceProperties = binder.bind(DEFAULT_DATASOURCE_PREFIX, Map.class).get();
        if (dataSourceConf != null) {
            Map<String, Object> dataSourceConfMap = DataTypeConvertUtil.beanToMap(dataSourceConf);
            dataSourceProperties.putAll(dataSourceConfMap);
        }
        DataSource dataSource = sundialProperties.getDatasourceType().newInstance();
        bind(dataSource, dataSourceProperties);
        return dataSource;
    }

    @SuppressWarnings("rawtypes")
    private void bind(DataSource result, Map properties) {
        ConfigurationPropertySource source = new MapConfigurationPropertySource(properties);
        Binder binder = new Binder(source.withAliases(ALIASES));
        binder.bind(ConfigurationPropertyName.EMPTY, Bindable.ofInstance(result));
    }
}
