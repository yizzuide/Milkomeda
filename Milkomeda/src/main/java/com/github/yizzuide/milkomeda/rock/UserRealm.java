package com.github.yizzuide.milkomeda.rock;

import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @date: 2019/12/6
 * @author: jsq
 * @email: 786063250@qq.com
 */

public class UserRealm extends AuthorizingRealm {

    /**
     * 授权(验证权限时调用)
     * 获取用户权限集合
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {

       principalCollection.getPrimaryPrincipal();
        List<String> permsList;
        Set<String> permsSet = new HashSet<>();
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.setStringPermissions(permsSet);
        return info;
    }

    /**
     * 认证(登录时调用)
     * 验证用户登录
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken token = (UsernamePasswordToken)authenticationToken;
        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo (null, null,  null,  getName());
        return info;
    }
}
