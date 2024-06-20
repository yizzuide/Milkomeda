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
import org.springframework.transaction.annotation.EnableTransactionManagement;
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
@EnableTransactionManagement
@AutoConfigureAfter(TransactionAutoConfiguration.class)
@EnableConfigurationProperties(TransactionProperties.class)
@ConditionalOnProperty(prefix = "milkomeda.hydrogen.transaction", name = "enable", havingValue = "true")
public class TransactionConfig {

    @Autowired
    private TransactionProperties props;

    // KP：PlatformTransactionManager是事务规范接口（实现有JDBC的DataSourceTransactionManager等），事务由具体数据库来现实，
    //  而TransactionDefinition和TransactionStatus这两个接口分别是事务的定义和运行状态。
    // Spring的事务通过AOP动态代理：TransactionInterceptor.invoke() -> TransactionAspectSupport.invokeWithinTransaction()
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    public TransactionInterceptor txAdvice(PlatformTransactionManager transactionManager) {
        RuleBasedTransactionAttribute txAttr_REQUIRED = new RuleBasedTransactionAttribute();
        // 设置传播行为：
        // PROPAGATION_REQUIRED：若当前存在事务，则加入该事务；如果当前没有事务，则创建一个新的事务。
        // PROPAGATION_REQUIRED_NEW：如果当前没有事务，创建一个新的事务；如果当前存在事务，则把当前事务挂起，再创建新事务，使执行相互独立。
        //  事务回滚原则：事务A调用事务B，事务B抛出异常回滚，由于没捕获被事务A监听到而导致事务A也回滚
        // PROPAGATION_NESTED：如果当前存在事务，则创建一个事务作为当前事务的嵌套事务来运行；如果当前没有事务，则该取值等价于PROPAGATION_REQUIRED。
        //  事务回滚原则：外部主事务回滚的话，子事务也会回滚，而内部子事务可以单独回滚而不影响外部主事务和其他子事务。
        txAttr_REQUIRED.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        // 抛出异常后执行切点回滚
        txAttr_REQUIRED.setRollbackRules(props.getRollbackWhenException()
                .stream().map(RollbackRuleAttribute::new).collect(Collectors.toList()));
        // 设置隔离级别
        // ISOLATION_READ_UNCOMMITTED：读未提交，可能会产生脏读、不可重复读（同一事务多次读取记录值时不一样，重点在修改）、幻读（多次读取记录条数不一样，重在插入和删除）。
        // ISOLATION_READ_COMMITTED：读已提交，可能会产生不可重复读、幻读。
        // ISOLATION_REPEATABLE_READ：可重复读，一定程度上解决了产生幻读问题（除非事务使用更新类型的当前读时会重新生成 ReadView，从而导致幻读）。
        //  InnoDB存储引擎在 REPEATABLE-READ（可重读）事务隔离级别下使用的是 Next-Key Lock 锁（记录锁 + Cap锁），且不会造成任何性能上的损失。
        // ISOLATION_SERIALIZABLE：串行化，完全遵行ACID，解决所有问题，但性能会大幅下降。
        txAttr_REQUIRED.setIsolationLevel(props.getIsolationLevel().value());
        // 设置超时
        txAttr_REQUIRED.setTimeout((int) props.getRollbackWhenTimeout().getSeconds());

        // 为什么要有只读事务？
        // 由于MySQL默认对每一个新建立的连接都启用了autocommit模式。在该模式下，每一个发送到 MySQL 服务器的sql语句都会在一个单独的事务中进行处理，执行结束后会自动提交事务。
        // 如果不加@Transactional，每条sql会开启一个单独的事务，中间被其它事务改了数据，都会实时读取到最新值，这样会导致数据不一致。
        RuleBasedTransactionAttribute txAttr_REQUIRED_READONLY = new RuleBasedTransactionAttribute();
        txAttr_REQUIRED_READONLY.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        txAttr_REQUIRED_READONLY.setReadOnly(true);

        NameMatchTransactionAttributeSource source = new NameMatchTransactionAttributeSource();
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
