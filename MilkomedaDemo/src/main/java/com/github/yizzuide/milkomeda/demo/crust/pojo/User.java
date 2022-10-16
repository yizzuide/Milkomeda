package com.github.yizzuide.milkomeda.demo.crust.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.yizzuide.milkomeda.crust.CrustEntity;
import com.github.yizzuide.milkomeda.crust.CrustPermission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * User
 * 用户表映射实体
 *
 * @author yizzuide
 * <br>
 * Create at 2019/11/11 23:46
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
// 实现CrustEntity适配接口，提供给Spring Security验证
public class User implements CrustEntity {
    private static final long serialVersionUID = -9190491929257431915L;
    private String id;
    private String username;
    @JsonIgnore
    private String password;
    @JsonIgnore
    private String salt;

    @Override
    public Serializable getUId() {
        return id;
    }

    @Override
    public void setPermissionList(List<? extends CrustPermission> permissionList) {
    }

    @Override
    public List<? extends CrustPermission> getPermissionList() {
        return null;
    }
}
