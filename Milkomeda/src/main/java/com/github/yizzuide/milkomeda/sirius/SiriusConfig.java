/*
 * Copyright (c) 2022 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.sirius;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.MybatisXMLConfigBuilder;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler;
import com.baomidou.mybatisplus.core.handlers.StrictFill;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.github.yizzuide.milkomeda.sirius.wormhole.SiriusInspector;
import com.github.yizzuide.milkomeda.sirius.wormhole.SiriusTransactionWorkBus;
import com.github.yizzuide.milkomeda.universe.extend.env.CollectionsPropertySource;
import com.github.yizzuide.milkomeda.universe.extend.env.SpELPropertySource;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import com.github.yizzuide.milkomeda.wormhole.TransactionWorkBus;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;

/**
 * Sirius module config.
 * 基于YML配置零代码全自动式Mybatis-plus字段填充插件，再也不用添加属性注解@TableField
 *
 * @see MybatisConfiguration
 * @see MybatisSqlSessionFactoryBean
 * @see MybatisSqlSessionFactoryBuilder
 * @since 3.14.0
 * @version 4.0.0
 * @author yizzuide
 * <br>
 * Create at 2022/10/30 17:52
 */
@AutoConfigureBefore(MybatisPlusAutoConfiguration.class)
@EnableConfigurationProperties({SiriusProperties.class, TenantProperties.class})
@Configuration
public class SiriusConfig {

    @Autowired
    private SiriusProperties props;

    @Autowired
    private TenantProperties tenantProperties;

    @Bean
    public SiriusInspector siriusInspector() {
        return new SiriusInspector();
    }

    @Bean
    public TransactionWorkBus transactionWorkBus() {
        return new SiriusTransactionWorkBus();
    }

    @Bean
    public BatchInjector batchInjector() {
        return new BatchInjector();
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        // 分页插件
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor(props.getDbType()));
        // 防全表更新插件
        mybatisPlusInterceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        // 添加SaaS租户插件
        if (tenantProperties.isEnable()) {
            TenantInterceptHandler tenantInterceptHandler = new TenantInterceptHandler(tenantProperties);
            SiriusHolder.setTenantInterceptHandler(tenantInterceptHandler);
            mybatisPlusInterceptor.addInnerInterceptor(new TenantLineInnerInterceptor(tenantInterceptHandler));
        }
        return mybatisPlusInterceptor;
    }

    // 属性自定义配置，在Mybatis-plus自动配置前执行
    @Bean
    public MybatisPlusPropertiesCustomizer propertiesCustomizer(ResourceLoader resourceLoader) throws IOException {
        return properties -> {
            GlobalConfig globalConfig = properties.getGlobalConfig();
            globalConfig.setBanner(false);
            if (properties.getConfiguration() == null) {
                // 如果有设置XML配置
                if (properties.getConfigLocation() != null) {
                    Resource resource = resourceLoader.getResource(properties.getConfigLocation());
                    MybatisXMLConfigBuilder xmlConfigBuilder;
                    try {
                        xmlConfigBuilder = new MybatisXMLConfigBuilder(resource.getInputStream(), null,
                                properties.getConfigurationProperties());
                    } catch (IOException e) {
                        throw new RuntimeException("Sirius load mybatis xml config error", e);
                    }
                    // ConfigLocation -> Mybatis Configuration
                    properties.setConfigLocation(null);
                    MybatisPlusProperties.CoreConfiguration coreConfiguration = new MybatisPlusProperties.CoreConfiguration();
                    coreConfiguration.applyTo(xmlConfigBuilder.getConfiguration());
                    properties.setConfiguration(coreConfiguration);
                    return;
                }
                // 没有设置过，创建默认配置
                MybatisPlusProperties.CoreConfiguration coreConfiguration = new MybatisPlusProperties.CoreConfiguration();
                coreConfiguration.setDefaultEnumTypeHandler(MybatisEnumTypeHandler.class);
                properties.setConfiguration(coreConfiguration);
            }
        };
    }

    // 扩展Mybatis Configuration配置，开发者可照样在应用层定义多个ConfigurationCustomizer Bean（优先级大于XML配置）
    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> configuration.setDefaultScriptingLanguage(SiriusMybatisXMLLanguageDriver.class);
    }

    @Bean
    public MybatisPlusMetaObjectHandler mybatisPlusMetaObjectHandler() {
        return new MybatisPlusMetaObjectHandler();
    }

    @Getter
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

        @Autowired
        private SiriusProperties props;

        private List<String> insertFields;

        private List<String> updateFields;

        @PostConstruct
        public void init() {
            List<SiriusProperties.AutoInterpolate> autoInterpolates = props.getAutoInterpolates();
            if (CollectionUtils.isEmpty(autoInterpolates)) {
                return;
            }
            autoInterpolates.forEach(autoInterpolate -> appendFields(
                    autoInterpolate.getFieldFill() != FieldFill.UPDATE,
                    autoInterpolate.getFieldFill() == FieldFill.UPDATE ||
                            autoInterpolate.getFieldFill() == FieldFill.INSERT_UPDATE, autoInterpolate));
        }

        private void appendFields(boolean insertFill, boolean updateFill, SiriusProperties.AutoInterpolate autoInterpolate) {
            if (insertFill) {
                if (insertFields == null) {
                    insertFields = new ArrayList<>(autoInterpolate.getFields());
                } else {
                    insertFields.addAll(autoInterpolate.getFields());
                }
            }
            if (updateFill) {
                if (updateFields == null) {
                    updateFields = new ArrayList<>(autoInterpolate.getFields());
                } else {
                    updateFields.addAll(autoInterpolate.getFields());
                }
            }
        }

        @Override
        public void insertFill(MetaObject metaObject) {
            executeFill(true, metaObject, insertFields);
        }


        @Override
        public void updateFill(MetaObject metaObject) {
            executeFill(false, metaObject, updateFields);
        }

        @SuppressWarnings("unchecked")
        private void executeFill(boolean insertFill, MetaObject metaObject, List<String> fields) {
            if (CollectionUtils.isEmpty(fields)) {
                return;
            }
            fields.forEach(fieldName -> {
                if(!metaObject.hasGetter(fieldName)) {
                    return;
                }
                Optional<Object> valueOp = Optional.ofNullable(findPsValue(fieldName));
                if (valueOp.isPresent()) {
                    Object value = valueOp.get();
                    Class<Object> aClass = (Class<Object>) value.getClass();
                    // convert value type
                    SiriusProperties.AutoInterpolate interpolate = findInterpolate(fieldName);
                    if (interpolate != null && interpolate.getConverterClazz() != null) {
                        GenericConverter converter = ReflectUtil.newInstance(interpolate.getConverterClazz());
                        if (converter != null) {
                            TableInfo tableInfo = this.findTableInfo(metaObject);
                            for (TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
                                if (fieldInfo.getProperty().equals(fieldName)) {
                                    Class<?> targetType = fieldInfo.getPropertyType();
                                    value = converter.convert(value, TypeDescriptor.valueOf(aClass), TypeDescriptor.valueOf(targetType));
                                    if (value != null) {
                                        aClass = (Class<Object>) value.getClass();
                                    }
                                }
                            }
                        }
                    }

                    if (insertFill) {
                        this.strictInsertFill(metaObject, fieldName, aClass, value);
                    } else {
                        this.strictUpdateFill(metaObject, fieldName, aClass, value);
                    }
                }
            });
        }

        @Override
        public MetaObjectHandler strictFill(boolean insertFill, TableInfo tableInfo, MetaObject metaObject, List<StrictFill<?, ?>> strictFills) {
            if (props.isAutoAddFill()) {
                strictFills.forEach(i -> {
                    final String fieldName = i.getFieldName();
                    final Class<?> fieldType = i.getFieldType();
                    tableInfo.getFieldList().stream()
                            .filter(j -> j.getProperty().equals(fieldName) && fieldType.equals(j.getPropertyType())).findFirst()
                            .ifPresent(j -> {
                                conditionFill(insertFill, j);
                                strictFillStrategy(metaObject, fieldName, i.getFieldVal());
                            });
                });
                return this;
            }
            return MetaObjectHandler.super.strictFill(insertFill, tableInfo, metaObject, strictFills);
        }

        private SiriusProperties.AutoInterpolate findInterpolate(@NonNull String fieldName) {
            List<SiriusProperties.AutoInterpolate> autoInterpolates = props.getAutoInterpolates();
            for (SiriusProperties.AutoInterpolate autoInterpolate : autoInterpolates) {
                if (autoInterpolate.getFields().contains(fieldName)) {
                    return autoInterpolate;
                }
            }
            return null;
        }

        @Nullable
        public Object findPsValue(@NonNull String fieldName) {
            SiriusProperties.AutoInterpolate selectAutoInterpolate = findInterpolate(fieldName);
            if (selectAutoInterpolate != null) {
                Object psValue = CollectionsPropertySource.of(selectAutoInterpolate.getPsValue());
                if (!psValue.equals(selectAutoInterpolate.getPsValue())) {
                    return psValue;
                }
                try {
                    psValue = SpELPropertySource.parseElFun(selectAutoInterpolate.getPsValue());
                } catch (Exception ignore) {
                    psValue = selectAutoInterpolate.getDefaultValue();
                }
                if (psValue != null) {
                    return psValue;
                }
                return selectAutoInterpolate.getPsValue();
            }
            return null;
        }

        public void conditionFill(boolean insertFill, @NonNull Object target) {
            String propKey = "withUpdateFill";
            List<String> fillFields = this.getUpdateFields();
            if (insertFill) {
                propKey = "withInsertFill";
                fillFields = this.getInsertFields();
            }
            if (target instanceof TableInfo tableInfo) {
                for (TableFieldInfo fieldInfo: tableInfo.getFieldList()) {
                    if (fillFields.contains(fieldInfo.getProperty())) {
                        Map<String, Object> props = new HashMap<>();
                        props.put(propKey, true);
                        ReflectUtil.setField(target, props);
                        return;
                    }
                }
            } else {
                TableFieldInfo tableFieldInfo = (TableFieldInfo)target;
                if (fillFields.contains(tableFieldInfo.getProperty())) {
                    Map<String, Object> props = new HashMap<>();
                    props.put(propKey, true);
                    ReflectUtil.setField(target, props);
                }
            }
        }
    }
}
