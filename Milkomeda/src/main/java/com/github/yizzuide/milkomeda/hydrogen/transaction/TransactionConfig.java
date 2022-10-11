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

package com.github.yizzuide.milkomeda.hydrogen.transaction;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.util.CollectionUtils;

import java.util.stream.Collectors;

/**
 * TransactionConfig
 * 切面事务配置
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.14.0
 * <br>
 * Create at 2019/11/25 10:56
 */
@Aspect
@Configuration
@EnableConfigurationProperties(TransactionProperties.class)
@AutoConfigureAfter(TransactionAutoConfiguration.class)
@ConditionalOnProperty(prefix = "milkomeda.hydrogen.transaction", name = "enable", havingValue = "true")
public class TransactionConfig {

    @Autowired
    private TransactionProperties props;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    public TransactionInterceptor txAdvice(PlatformTransactionManager transactionManager) {
        RuleBasedTransactionAttribute txAttr_REQUIRED = new RuleBasedTransactionAttribute();
        // 设置传播行为：若当前存在事务，则加入该事务；如果当前没有事务，则创建一个新的事务。这是默认值。
        txAttr_REQUIRED.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        // 抛出异常后执行切点回滚
        txAttr_REQUIRED.setRollbackRules(props.getRollbackWhenException()
                .stream().map(RollbackRuleAttribute::new).collect(Collectors.toList()));
        // 设置超时
        txAttr_REQUIRED.setTimeout((int) props.getRollbackWhenTimeout().getSeconds());

        RuleBasedTransactionAttribute txAttr_REQUIRED_READONLY = new RuleBasedTransactionAttribute();
        txAttr_REQUIRED_READONLY.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        txAttr_REQUIRED_READONLY.setReadOnly(true);

        NameMatchTransactionAttributeSource source = new NameMatchTransactionAttributeSource();
        // 开启只读, 提高数据库访问性能
        if (!CollectionUtils.isEmpty(props.getReadOnlyPrefix())) {
            for (String prefix : props.getReadOnlyPrefix()) {
                source.addTransactionalMethod(prefix, txAttr_REQUIRED_READONLY);
            }
        }

        if (!CollectionUtils.isEmpty(props.getReadOnlyAppendPrefix())) {
            for (String prefix : props.getReadOnlyAppendPrefix()) {
                source.addTransactionalMethod(prefix, txAttr_REQUIRED_READONLY);
            }
        }

        // 其它都需要事务
        source.addTransactionalMethod("*", txAttr_REQUIRED);
        return new TransactionInterceptor((TransactionManager) transactionManager, source);
    }

    @Bean
    public Advisor txAdviceAdvisor(TransactionInterceptor txAdvice) {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(props.getPointcutExpression());
        return new DefaultPointcutAdvisor(pointcut, txAdvice);
    }
}
