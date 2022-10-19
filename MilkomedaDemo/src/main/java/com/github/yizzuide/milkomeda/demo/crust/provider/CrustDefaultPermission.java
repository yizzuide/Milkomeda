package com.github.yizzuide.milkomeda.demo.crust.provider;

import com.github.yizzuide.milkomeda.crust.CrustPermission;
import lombok.Data;

import java.util.List;

/**
 * SysPermission
 *
 * @author yizzuide
 * <br>
 * Create at 2022/10/19 22:41
 */
@Data
public class CrustDefaultPermission implements CrustPermission {
    private Long id;

    private Long parentId;

    private String label;

    private String icon;

    private String code;

    private Integer type;

    private String routeName;

    private String routePath;

    private String componentPath;

    private int order;

    private List<CrustPermission> children;

    public static CrustPermission createRole(String role) {
        CrustPermission perm = new CrustDefaultPermission();
        perm.setCode(role);
        return perm;
    }
}
