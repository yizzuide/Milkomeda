package com.github.yizzuide.milkomeda.universe.ognl;

import lombok.Data;
import ognl.MemberAccess;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * OgnlMemberAccess
 *
 * @author yizzuide
 * @since 3.5.0
 * Create at 2020/05/20 13:51
 */
@Data
public class OgnlMemberAccess implements MemberAccess {
    private boolean allowPrivateAccess = false;
    private boolean allowProtectedAccess = false;
    private boolean allowPackageProtectedAccess = false;

    public OgnlMemberAccess(boolean allowAllAccess) {
        this(allowAllAccess, allowAllAccess, allowAllAccess);
    }

    public OgnlMemberAccess(boolean allowPrivateAccess, boolean allowProtectedAccess, boolean allowPackageProtectedAccess) {
        super();
        this.allowPrivateAccess = allowPrivateAccess;
        this.allowProtectedAccess = allowProtectedAccess;
        this.allowPackageProtectedAccess = allowPackageProtectedAccess;
    }

    public Object setup(Map context, Object target, Member member, String propertyName) {
        Object result = null;
        if (isAccessible(context, target, member, propertyName)) {
            AccessibleObject accessible = (AccessibleObject) member;
            if (!accessible.isAccessible()) {
                result = Boolean.FALSE;
                accessible.setAccessible(true);
            }
        }
        return result;
    }

    public void restore(Map context, Object target, Member member, String propertyName, Object state) {
        if (state != null) {
            ((AccessibleObject) member).setAccessible((Boolean) state);
        }
    }

    @Override
    public boolean isAccessible(Map context, Object target, Member member, String propertyName) {
        int modifiers = member.getModifiers();
        boolean result = Modifier.isPublic(modifiers);
        if (!result) {
            if (Modifier.isPrivate(modifiers)) {
                result = isAllowPrivateAccess();
            } else {
                if (Modifier.isProtected(modifiers)) {
                    result = isAllowProtectedAccess();
                } else {
                    result = isAllowPackageProtectedAccess();
                }
            }
        }
        return result;
    }
}
