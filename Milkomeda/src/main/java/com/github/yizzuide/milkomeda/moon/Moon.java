package com.github.yizzuide.milkomeda.moon;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Moon
 *
 * @author yizzuide
 * @since 2.2.1
 * Create at 2019/12/31 18:13
 */
public class Moon<T> {
    private MoonNode<T> pointer;
    private MoonNode<T> header;
    private MoonNode<T> next;
    // 指针锁
    private ReentrantLock reentrantLock = new ReentrantLock(false);

    /**
     * 在不同的业务流程获取当前阶段的类型值
     * @param name          业务名
     * @param moonPrototype 原型
     * @param <T>           阶段的类型
     * @return  当前阶段的类型值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getPhase(String name, Moon<T> moonPrototype) {
        Moon<T> moon = WebContext.registerBean((ConfigurableApplicationContext) ApplicationContextHolder.get(), name, Moon.class);
        // 新的流程创建的实例
        if (moon.getPointer() == null) {
            // 重置指针
            moon.setPointer(moonPrototype.getHeader());
        }
        return moon.getCurrentPhase();
    }

    /**
     * 添加阶段名
     * @param phaseNames    阶段列表
     */
    @SafeVarargs
    public final void add(T... phaseNames) {
        int length = phaseNames.length;
        for (int i = 0; i < length; i++) {
            if (i == 0) {
                header = new MoonNode<>();
                header.setData(phaseNames[i]);
                next = header;
                continue;
            }
            MoonNode<T> moonNode = new MoonNode<>();
            moonNode.setData(phaseNames[i]);
            next.setNext(moonNode);
            next = moonNode;
        }
        // 尾连首
        next.setNext(header);
        // 指向首
        pointer = header;
    }

    /**
     * 获得当前阶段类型
     * @return 阶段类型值
     */
    public T getCurrentPhase() {
        reentrantLock.lock();
        T data = pointer.getData();
        pointer = pointer.getNext();
        reentrantLock.unlock();
        return data;
    }

    MoonNode<T> getPointer() {
        return pointer;
    }

    void setPointer(MoonNode<T> pointer) {
        this.pointer = pointer;
    }

    MoonNode<T> getHeader() {
        return header;
    }
}
