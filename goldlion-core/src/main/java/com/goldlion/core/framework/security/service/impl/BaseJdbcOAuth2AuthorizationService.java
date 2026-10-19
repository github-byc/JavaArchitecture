package com.goldlion.core.framework.security.service.impl;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;

/**
 * 授权信息存储配置
 */
@Service
public class BaseJdbcOAuth2AuthorizationService extends JdbcOAuth2AuthorizationService {
    public BaseJdbcOAuth2AuthorizationService(JdbcOperations jdbcOperations, RegisteredClientRepository registeredClientRepository) {
        super(jdbcOperations, registeredClientRepository);
    }
}
