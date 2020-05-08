package com.github.yizzuide.milkomeda.sundial;

import com.github.yizzuide.milkomeda.util.DataTypeConvertUtil;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyNameAliases;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static jdk.nashorn.internal.objects.NativeFunction.bind;

/**
 * @date: 2020/5/8
 * @author: jsq
 * @email: 786063250@qq.com
 * @describe: 动态创建数据源工厂
 */
@Slf4j
@Configuration
public class DataSourceFactory implements EnvironmentAware {

    @Resource
    private SundialProperties sundialProperties;


    private static final String MYSQL_NAME = "${config.mysql.name}";
    private static final String NAME_KEY = "config.mysql.name";
    private static final String MYSQL_HOST_URL = "${config.mysql.hosturl}";
    private static final String HOST_URL_KEY = "config.mysql.hosturl";
    private static final String DEFAULT_DATASOURCE_URL = "spring.datasource.url";
    public static final String DEFAULT_DATASOURCE_PREFIX = "spring.datasource";

    private Environment evn;

    /**
     * 参数绑定工具
     */
    private Binder binder;

    /**
     * 格式化
     */
    private static final ConfigurationPropertyNameAliases ALIASES = new ConfigurationPropertyNameAliases();


    /**
     * 动态创建数据源
     * @param dataSourceName
     * @return
     */
    public DataSource create(String dataSourceName) throws IllegalAccessException, InstantiationException {

        // url 组装
        String url = urlProcessor(dataSourceName);
        // 创建数据源绑定参数
        Map dataSourceProperties = binder.bind(DEFAULT_DATASOURCE_PREFIX, Map.class).get();
        DataSource datasource = sundialProperties.getDatasourceType().newInstance();
        bind(datasource, dataSourceProperties);
        Map<String,Object> props = new HashMap<>();
        props.put("url",url);
        ReflectUtil.setField(datasource,props);
        log.info("[createDataSource] Generate {} DruidDataSource", dataSourceName);
        return datasource;
    }


    /**
     * 根据配置信息构建
     * @param config
     * @return
     */
    public DataSource createDataSource(SundialProperties.Datasource config) throws IllegalAccessException, InstantiationException {
        // 创建数据源绑定参数
        Map dataSourceProperties = binder.bind(DEFAULT_DATASOURCE_PREFIX, Map.class).get();
        DataSource datasource = sundialProperties.getDatasourceType().newInstance();
        bind(datasource, dataSourceProperties);

        // url 组装
        String url = urlMaking(config.getUrl(), config.getUsername());
        Map<String,Object> props = new HashMap<>();
        props.put("url",url);
        props.put("username",config.getUsername());
        props.put("password",config.getPassword());
        ReflectUtil.setField(datasource,props);
        log.info("[createDataSource] Generate {} datasource",datasource);
        return datasource;
    }


    /**
     * 获取环境配置的URL
     * @param dataSourceName
     * @return
     */
    private String urlProcessor(String dataSourceName){
        String url = evn.getProperty(DEFAULT_DATASOURCE_URL);
        if (StringUtils.isEmpty(url)) {
            throw new IllegalArgumentException(
                    String.format("[createDataSource] %s data source url is null", DEFAULT_DATASOURCE_URL));
        }

        // 数据库名称替换策略
        if (dataSourceName != null) {
            String defaultName = evn.getProperty(NAME_KEY);
            if (!StringUtils.isEmpty(defaultName)) {
                url = url.replace(defaultName, dataSourceName);
            } else {
                url = url.replace(MYSQL_NAME, dataSourceName);
            }
        }
        return url;
    }

    /**
     * 拼接数据库URL
     * @param hostUrl
     * @param dataSourceName
     * @return
     */
    public String urlMaking(String hostUrl, String dataSourceName) {
        String url = evn.getProperty(DEFAULT_DATASOURCE_URL);
        if (StringUtils.isEmpty(url)) {
            throw new IllegalArgumentException(
                    String.format("[createDataSource] %s data source url is null", DEFAULT_DATASOURCE_URL));
        }
        if (StringUtils.isEmpty(hostUrl)) {
            throw new IllegalArgumentException("[createDataSource] hostUrl is null");
        }
        if (StringUtils.isEmpty(dataSourceName)) {
            throw new IllegalArgumentException("[createDataSource] dataSourceName is null");
        }
        StringBuilder sb = new StringBuilder("jdbc:mysql://");
        sb.append(hostUrl).append("/").append(dataSourceName);
        int i = url.indexOf("?");
        if (i != -1) {
            sb.append(url.substring(i));
        }
        return sb.toString();
    }



    @Override
    public void setEnvironment(Environment environment) {

    }
}
