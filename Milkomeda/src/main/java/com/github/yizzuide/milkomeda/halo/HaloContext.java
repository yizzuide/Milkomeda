package com.github.yizzuide.milkomeda.halo;

import com.github.yizzuide.milkomeda.universe.context.AopContextHolder;
import com.github.yizzuide.milkomeda.universe.metadata.HandlerMetaData;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HaloContext
 * 上下文信息
 *
 * @author yizzuide
 * @since 2.5.0
 * @version 3.11.1
 * Create at 2020/01/30 22:40
 * @see org.springframework.context.support.AbstractApplicationContext#refresh()
 */
public class HaloContext implements ApplicationListener<ContextRefreshedEvent> {
    /**
     * 监听类型的方法属性名
     */
    public static final String ATTR_TYPE = "type";
    /**
     * 调用处理异步方式的方法属性名
     */
    public static final String ATTR_ASYNC = "async";

    private static Map<String, List<HandlerMetaData>> tableNameMap = new HashMap<>();

    private static final Map<String, List<HandlerMetaData>> preTableNameMap = new HashMap<>();

    private static final Map<String, List<HandlerMetaData>> postTableNameMap = new HashMap<>();

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        tableNameMap = AopContextHolder.getHandlerMetaData(HaloHandler.class, HaloListener.class, (annotation, handlerAnnotation, metaData) -> {
            HaloListener haloListener = (HaloListener) annotation;
            boolean isAsyncPresentOn = metaData.getMethod().isAnnotationPresent(Async.class);
            // 设置其它属性方法的值
            Map<String, Object> attrs = new HashMap<>(4);
            attrs.put(ATTR_TYPE, haloListener.type());
            attrs.put(ATTR_ASYNC, isAsyncPresentOn || haloListener.async());
            metaData.setAttributes(attrs);
            String value = haloListener.value();
            cacheWithType(haloListener.type() == HaloType.PRE ? preTableNameMap : postTableNameMap, value, metaData);
            return value;
        }, false);
    }

    private void cacheWithType(Map<String, List<HandlerMetaData>> map, String tableName, HandlerMetaData metaData) {
        if (map.get(tableName) == null) {
            List<HandlerMetaData> handlers = new ArrayList<>();
            handlers.add(metaData);
            map.put(tableName, handlers);
        } else {
            map.get(tableName).add(metaData);
        }
    }

    static Map<String, List<HandlerMetaData>> getTableNameMap() {
        return tableNameMap;
    }

    static Map<String, List<HandlerMetaData>> getPostTableNameMap() {
        return postTableNameMap;
    }

    static Map<String, List<HandlerMetaData>> getPreTableNameMap() {
        return preTableNameMap;
    }
}
