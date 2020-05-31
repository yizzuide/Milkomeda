package com.github.yizzuide.milkomeda.sundial;

import com.github.yizzuide.milkomeda.util.ReflectUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import java.lang.reflect.Field;

/**
 * SundialTweakConfig
 *
 * @author yizzuide
 * @since 3.7.1
 * Create at 2020/05/31 14:57
 */
@ConditionalOnClass(MybatisAutoConfiguration.class)
@AutoConfigureAfter(MybatisAutoConfiguration.class)
public class SundialTweakConfig {
    @Autowired
    public void configSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        MultiDataSourceTransactionFactory multiDataSourceTransactionFactory = new MultiDataSourceTransactionFactory();
        Pair<Field, Object> envFieldBundle = ReflectUtil.getFieldBundlePath(sqlSessionFactory, "configuration.environment");
        Environment originEnvironment = (Environment) envFieldBundle.getValue();
        Environment environment = new Environment(originEnvironment.getId(), multiDataSourceTransactionFactory, originEnvironment.getDataSource());
        Pair<Field, Object> confFieldBundle = ReflectUtil.getFieldBundlePath(sqlSessionFactory, "configuration");
        Object configuration = confFieldBundle.getValue();
        ReflectUtil.invokeMethod(configuration, "setEnvironment", new Class[]{Environment.class}, environment);
    }
}
