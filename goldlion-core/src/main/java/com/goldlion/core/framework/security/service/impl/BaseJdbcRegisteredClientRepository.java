package com.goldlion.core.framework.security.service.impl;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.stereotype.Service;

/**
 * 注册客户端管理
 */
@Service
public class BaseJdbcRegisteredClientRepository extends JdbcRegisteredClientRepository {
    public BaseJdbcRegisteredClientRepository(JdbcOperations jdbcOperations) {
        super(jdbcOperations);
    }
}
