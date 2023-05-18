/*
 * Copyright (c) 2023 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.universe.aop.invoke.args;

import java.lang.reflect.Method;
import java.util.List;

/**
 * A method based argument binder for reflection invoke which auto bind args value at corresponding index with {@link ArgumentDefinition} config.
 *
 * @see org.springframework.aop.aspectj.AbstractAspectJAdvice#calculateArgumentBindings()
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/01/11 19:21
 */
public class MethodArgumentBinder {

    // volatile变量规则：对一个volatile变量的写，happens-before于任意后续对这个volatile变量的读。
    // happens-before：如果一个操作happens-before另一个操作，那么第一个操作的执行结果将对第二个操作可见。
    // 实现原理：为实现volatile内存语义，JMM会限制编译器重排序和处理器重排序（编译器在生成字节码时，会在指令序列中插入内存屏障来禁止处理器重排序）。
    // 读写操作：当操作volatile变量写时，JMM把该线程的本地内存（CPU缓存、寄存器）中的所有共享变量刷新到主内存；当读的时候，volatile变量强制从主内存中读取。
    // 应用技巧：释放锁的线程在写volatile变量之前对共享变量进行的修改，在别的线程读取同一个volatile变量后将立即可见。
    private static volatile CompositeArgumentMatcher argumentMatchers;

    /**
     * Get bind args from argument definition list.
     * @param argumentSources   argument definition list
     * @param method    target method
     * @return args
     */
    public static Object[] bind(ArgumentSources argumentSources, Method method) {
        CompositeArgumentMatcher argumentMatchers = getArgumentMatchers();
        List<ArgumentDefinition> argumentDefinitions = argumentSources.getList();
        int size = method.getParameters().length;
        Object[] args = new Object[size];
        for (ArgumentDefinition argumentDefinition : argumentDefinitions) {
            if (argumentMatchers.support(argumentDefinition)) {
                argumentMatchers.matchToAdd(args, method, argumentDefinition);
            }
        }
        return args;
    }

    /**
     * Register argument matcher to match special argument definition.
     * @param argumentMatcher   ArgumentMatcher
     */
    public static void register(ArgumentMatcher argumentMatcher) {
        getArgumentMatchers().addMatcher(argumentMatcher);
    }

    private static CompositeArgumentMatcher getArgumentMatchers() {
        if (argumentMatchers == null) {
            // 每个Object都关联一个Monitor（存在于对象头Mark Word），它记录被哪个线程获取、重入次数、block队列、wait队列等，同时notify/notifyAll/wait等方法会使用到Monitor锁对象，所以必须在同步代码块中使用。
            // Synchronized的同步是可重入、非公平抢占方式，在JVM里的实现都是基于MonitorEnter和MonitorExit指令进入退出Monitor对象来实现方法同步和代码块同步。
            synchronized (MethodArgumentBinder.class) {
                if (argumentMatchers == null) {
                    // new操作包含了三条指令：
                    // 1.分配对象内存M；2.在内存M上初始化对象；3.将M的地址赋值给变量。
                    // 如果指令优化重排，可能导致其它线程赋使用变量时是一个未初始化的对象而触发空指针异常。
                    argumentMatchers = new CompositeArgumentMatcher();
                    argumentMatchers.addMatcher(new NamedTypeArgumentMatcher());
                    argumentMatchers.addMatcher(new JDKRegexArgumentMatcher());
                    argumentMatchers.addMatcher(new ResidualArgumentMatcher());
                }
            }
        }
        return argumentMatchers;
    }
}
