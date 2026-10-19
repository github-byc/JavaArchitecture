package com.goldlion.core.framework.security.service;

import com.goldlion.core.framework.security.domain.model.OAuth2User;

/**
 * OAuth2登录 服务类
 */
public interface OAuth2LoginService {

    /**
     * 登录
     */
    String login(String username, String password);

    /**
     * 登出
     */
    void logout();

    /**
     * 根据token获取OAuth2用户
     */
    OAuth2User getOAuth2User(String token);

}
