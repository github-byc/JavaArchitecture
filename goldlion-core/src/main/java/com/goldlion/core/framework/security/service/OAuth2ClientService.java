package com.goldlion.core.framework.security.service;

import com.goldlion.core.framework.security.domain.model.OAuth2Client;

/**
 * OAuth2客户端 服务类
 */
public interface OAuth2ClientService {

    /**
     * 创建 OAuth2客户端
     */
    OAuth2Client create(OAuth2Client client);

    /**
     * 修改 OAuth2客户端
     */
    OAuth2Client update(OAuth2Client client);

    /**
     * 修改 OAuth2客户端秘钥
     */
    OAuth2Client reset(String clientId);

    /**
     * 获取 OAuth2客户端
     */
    OAuth2Client get(String clientId);

}
