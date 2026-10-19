package com.goldlion.core.middleware.redis.config;

import com.goldlion.core.middleware.redis.core.JsonRedisTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * redis 配置类
 */
@Configuration
public class RedisConfig {

    @ConditionalOnMissingBean(name = "jsonRedisTemplate")
    @Bean
    public JsonRedisTemplate jsonRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new JsonRedisTemplate(connectionFactory);
    }
}