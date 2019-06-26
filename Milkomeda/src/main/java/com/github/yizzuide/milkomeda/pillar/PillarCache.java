package com.github.yizzuide.milkomeda.pillar;

import java.util.HashMap;
import java.util.Map;

/**
 * PillarCache
 * 分流柱缓存
 *
 * @since 1.7.0
 * @author yizzuide
 * Create at 2019/06/26 10:50
 */
public class PillarCache<T> {
    /**
     * 缓存容器
     */
    private Map<Object, PillarAttachment<T, ?>> cacheMap = new HashMap<>();

    /**
     * 缓存附件
     * @param id            标识符
     * @param attachment    缓存的附件对象
     */
    public void set(Object id, PillarAttachment<T, ?> attachment) {
        cacheMap.put(id, attachment);
    }

    /**
     * 从缓存获取附件
     * @param id    标识符
     * @return      缓存的对象
     */
    @SuppressWarnings("unchecked")
    public <E> PillarAttachment<T, E> get(Object id) {
        return (PillarAttachment<T, E>) cacheMap.get(id);
    }

    /**
     * 获取缓存的分流柱
     * @param id    标识符
     * @return      缓存的分流柱
     */
    public T getPillar(Object id) {
        return cacheMap.get(id).pillar;
    }

    /**
     * 获取缓存的业务对象
     * @param id    标识符
     * @param <E>   业务对象类型
     * @return      缓存的业务对象
     */
    @SuppressWarnings("unchecked")
    public <E> E getData(Object id) {
        return (E) cacheMap.get(id).getData();
    }

    /**
     * 清空缓存
     */
    public void clear() {
        cacheMap.clear();
    }
}
