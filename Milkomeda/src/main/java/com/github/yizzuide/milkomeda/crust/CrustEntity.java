package com.github.yizzuide.milkomeda.crust;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;

/**
 * CrustEntity
 * 需要用户实体实现的适配接口
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 1.17.3
 * Create at 2019/11/11 18:47
 */
public interface CrustEntity extends Serializable {
    /**
     * 用户id
     * @return user id
     */
    @NonNull
    String getUID();

    /**
     * 用户名
     * @return username
     */
    @NonNull
    String getUsername();

    /**
     * 登录密码
     * @return password
     */
    @NonNull
    String getPassword();

    /**
     * 如果设置了<code>milkomeda.crust.use_bCrypt</code>为<code>true</code>(默认为true)，
     * 那个这个字段不需要实现，否则需要实现
     * @return salt
     */
    @Nullable
    default String getSalt() {return null;}
}
