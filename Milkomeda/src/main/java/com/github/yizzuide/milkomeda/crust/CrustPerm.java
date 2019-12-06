package com.github.yizzuide.milkomeda.crust;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * CrustRole
 *
 * @author yizzuide
 * @since 1.17.2
 * Create at 2019/12/06 17:17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrustPerm {
    /**
     * 角色ID列表
     */
    private Set<Long> roleIds;
    /**
     * 权限或角色名列表
     */
    private List<String> permNames;
}
