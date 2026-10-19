package com.goldlion.core.common.context;

import cn.hutool.core.util.StrUtil;
import com.goldlion.core.common.domain.model.AuthSecurityUser;

/**
 * 权限上下文
 */
public class SecurityContextHolder {

    private static final ThreadLocal<AuthSecurityUser> contextHolder = new ThreadLocal<>();

    public static AuthSecurityUser getContext() {
        return contextHolder.get();
    }

    public static void setContext(AuthSecurityUser context) {
        contextHolder.set(context);
    }

    public static void clearContext() {
        contextHolder.remove();
    }

    //==================================================================================================================

    public static String getCurrentUsername() {
        if (StrUtil.isNotBlank(getContext().getOperateMqUserName())) {
            return getContext().getOperateMqUserName();
        }
        return getContext().getLoginAuthUser().getUsername();
    }
}
