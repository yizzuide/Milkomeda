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

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.github.yizzuide.milkomeda.universe.extend.env.CollectionsPropertySource;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.function.Supplier;

/**
 * SiriusConfig
 *
 * @author yizzuide
 * <br>
 * Create at 2022/10/30 17:52
 */
@AutoConfigureAfter(MybatisPlusAutoConfiguration.class)
@EnableConfigurationProperties(SiriusProperties.class)
@Configuration
public class SiriusConfig {

    @Autowired
    private SiriusProperties props;

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        // 分页插件
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor(props.getDbType()));
        // 防全表更新插件
        mybatisPlusInterceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return mybatisPlusInterceptor;
    }

    // 自定义mybatis配置
    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> configuration.setMapUnderscoreToCamelCase(true);
    }

    @Bean
    public MybatisPlusMetaObjectHandler mybatisPlusMetaObjectHandler() {
        return new MybatisPlusMetaObjectHandler();
    }

    static class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

        @Autowired
        private SiriusProperties props;

        private List<String> insertFields;

        private List<String> updateFields;

        private Supplier<Object> insertValueProvider;

        private Supplier<Object> updateValueProvider;

        @PostConstruct
        public void init() {
            List<SiriusProperties.AutoInterpolate> autoInterpolates = props.getAutoInterpolates();
            if (CollectionUtils.isEmpty(autoInterpolates)) {
                return;
            }
            autoInterpolates.forEach(autoInterpolate -> {
                if (autoInterpolate.getModifyType() == ModifyType.INSERT) {
                    insertFields = autoInterpolate.getFields();
                    insertValueProvider = () -> CollectionsPropertySource.of(autoInterpolate.getPsValue());
                } else {
                    updateFields = autoInterpolate.getFields();
                    updateValueProvider = () -> CollectionsPropertySource.of(autoInterpolate.getPsValue());
                }
            });
        }

        @SuppressWarnings("unchecked")
        @Override
        public void insertFill(MetaObject metaObject) {
            if (CollectionUtils.isEmpty(insertFields)) {
                return;
            }
            insertFields.forEach(fieldName -> {
                Object value = insertValueProvider.get();
                Class<Object> aClass = (Class<Object>) value.getClass();
                this.strictInsertFill(metaObject, fieldName, aClass, value);
            });
        }

        @SuppressWarnings("unchecked")
        @Override
        public void updateFill(MetaObject metaObject) {
            if (CollectionUtils.isEmpty(updateFields)) {
                return;
            }
            updateFields.forEach(fieldName -> {
                Object value = updateValueProvider.get();
                Class<Object> aClass = (Class<Object>) value.getClass();
                this.strictInsertFill(metaObject, fieldName, aClass, value);
            });
        }
    }
}
