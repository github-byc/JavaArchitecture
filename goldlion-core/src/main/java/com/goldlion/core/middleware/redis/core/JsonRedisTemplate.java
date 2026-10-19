package com.goldlion.core.middleware.redis.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * json 格式 RedisTemplate
 */
public class JsonRedisTemplate extends RedisTemplate<String, Object> {

    public JsonRedisTemplate() {
        ObjectMapper mapper = new ObjectMapper();
        // 禁用时间戳格式化，使用ISO8601格式存储日期时间
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 注册JavaTimeModule以支持LocalDateTime等Java 8日期时间类型
        mapper.registerModule(new JavaTimeModule());
        // 配置忽略null值
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 使用Jackson2JsonRedisSerialize 替换默认的JDK Serializable序列化
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(mapper, Object.class);

        this.setKeySerializer(RedisSerializer.string());
        this.setValueSerializer(serializer);
        this.setHashKeySerializer(RedisSerializer.string());
        this.setHashValueSerializer(serializer);
    }

    public JsonRedisTemplate(RedisConnectionFactory connectionFactory) {
        this();
        this.setConnectionFactory(connectionFactory);
        this.afterPropertiesSet();
    }

}
