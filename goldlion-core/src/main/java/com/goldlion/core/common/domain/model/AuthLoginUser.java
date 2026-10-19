package com.goldlion.core.common.domain.model;

import java.util.Set;

/**
 * 身份登录用户
 */
public interface AuthLoginUser {

    /**
     * 获取用户名
     */
    String getUsername();

    /**
     * 获取密码
     */
    String getPassword();

    /**
     * 获取权限集合
     */
    Set<String> getScopes();

}
