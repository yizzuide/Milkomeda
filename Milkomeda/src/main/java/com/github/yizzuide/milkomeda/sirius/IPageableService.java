package com.github.yizzuide.milkomeda.sirius;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformPage;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformQueryPageData;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A pageable service extends from IService
 *
 * @since 3.14.0
 * @version 4.0.0
 * @author yizzuide
 * <br>
 * Create at 2022/10/30 17:35
 */
public interface IPageableService<T> extends IService<T> {

    /**
     * Query matcher default group name.
     */
    String DEFAULT_GROUP = "default";

    /**
     * Query page data with command.
     * @param pageableService   pageable service
     * @param queryPageData     UniformQueryPageData with a command type
     * @param command2EntityConverter   command to entity converter
     * @param entity2VoConverter        entity to vo converter
     * @return UniformPage
     * @param <T>   entity type
     * @param <C>   command type
     * @param <V>   vo type
     * @since 4.0.0
     */
    static <T, C, V> UniformPage<V> selectByPage(IPageableService<T> pageableService, UniformQueryPageData<C> queryPageData, Function<C, T> command2EntityConverter, Function<T, V> entity2VoConverter) {
        return selectByPage(pageableService, queryPageData, DEFAULT_GROUP, command2EntityConverter, entity2VoConverter);
    }

    /**
     * Query page data with command.
     * @param pageableService   pageable service
     * @param queryPageData     UniformQueryPageData with a command type
     * @param group             match group name
     * @param command2EntityConverter   command to entity converter
     * @param entity2VoConverter        entity to vo converter
     * @return UniformPage
     * @param <T>   entity type
     * @param <C>   command type
     * @param <V>   vo type
     * @since 4.0.0
     */
    static <T, C, V> UniformPage<V> selectByPage(IPageableService<T> pageableService, UniformQueryPageData<C> queryPageData, String group, Function<C, T> command2EntityConverter, Function<T, V> entity2VoConverter) {
        return selectByPage(pageableService, queryPageData, null, group, command2EntityConverter, entity2VoConverter);
    }

    /**
     * Query page data with command.
     * @param pageableService   pageable service
     * @param queryPageData     UniformQueryPageData with a command type
     * @param queryMatchData    match data
     * @param group             match group name
     * @param command2EntityConverter   command to entity converter
     * @param entity2VoConverter        entity to vo converter
     * @return UniformPage
     * @param <T>   entity type
     * @param <C>   command type
     * @param <V>   vo type
     * @since 4.0.0
     */
    static <T, C, V> UniformPage<V> selectByPage(IPageableService<T> pageableService, UniformQueryPageData<C> queryPageData, Map<String, Object> queryMatchData, String group, Function<C, T> command2EntityConverter, Function<T, V> entity2VoConverter) {
        return UniformPage.convert(pageableService.selectByPage(
                UniformQueryPageData.convert(queryPageData, command2EntityConverter),
                queryMatchData,
                group
        ), entity2VoConverter);
    }

    /**
     * Query by page data.
     * @param queryPageData page data
     * @return  UniformPage
     */
    UniformPage<T> selectByPage(UniformQueryPageData<T> queryPageData);

    /**
     * Query by page data and match group.
     * @param queryPageData page data
     * @param group match group name
     * @return  UniformPage
     * @since 3.15.0
     */
    UniformPage<T> selectByPage(UniformQueryPageData<T> queryPageData, String group);

    /**
     * Query by page data and match data.
     * @param queryPageData page data
     * @param queryMatchData match data
     * @return  UniformPage
     * @since 3.15.0
     */
    UniformPage<T> selectByPage(UniformQueryPageData<T> queryPageData, Map<String, Object> queryMatchData);

    /**
     * Query by page data, match data and group name.
     * @param queryPageData page data
     * @param queryMatchData match data
     * @param group match group name
     * @return  UniformPage
     * @since 3.15.0
     */
    UniformPage<T> selectByPage(UniformQueryPageData<T> queryPageData, Map<String, Object> queryMatchData, String group);

    /**
     * Remove record row before check it reference.
     * @param entity  entity which has key of id value
     * @return  false if it has referenced
     */
    boolean removeBeforeCheckRef(T entity);

    /**
     * Assign authority for the owner.
     * @param ownerId   owner id
     * @param itemIds   authority item id list
     * @param conditionProvider condition which find linked with the owner
     * @param generator create authority item row linked with the owner
     * @return  true if success
     */
    boolean assignAuthority(Serializable ownerId, List<? extends Serializable> itemIds,
                            SFunction<T, Object> conditionProvider,
                            Function<Serializable, T> generator);
}
