package com.goldlion.core.framework.security.service.impl;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;

/**
 * 授权准许存储配置
 */
@Service
public class BaseJdbcOAuth2AuthorizationConsentService extends JdbcOAuth2AuthorizationConsentService {
    public BaseJdbcOAuth2AuthorizationConsentService(JdbcOperations jdbcOperations, RegisteredClientRepository registeredClientRepository) {
        super(jdbcOperations, registeredClientRepository);
    }
}
