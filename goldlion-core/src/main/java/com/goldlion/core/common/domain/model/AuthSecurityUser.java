package com.goldlion.core.common.domain.model;

/**
 * 身份权限用户
 */
public interface AuthSecurityUser {

    /**
     * 获取登录验证用户
     */
    AuthLoginUser getLoginAuthUser();

    /**
     * 获取操作MQ用户名
     */
    String getOperateMqUserName();

    /**
     * 获取微信验证用户
     */
    Object getWxAuthUser();

}
