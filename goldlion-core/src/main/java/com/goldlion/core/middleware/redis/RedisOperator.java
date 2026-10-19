package com.goldlion.core.middleware.redis;

import cn.hutool.core.collection.CollUtil;
import com.goldlion.core.common.exception.BusinessException;
import com.goldlion.core.middleware.redis.core.JsonRedisTemplate;
import com.goldlion.core.middleware.redis.key.KeyPrefix;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * redis操作工具类
 */
@Component
public class RedisOperator {
    @Resource
    private JsonRedisTemplate jsonRedisTemplate;

    //==================================================================================================================

    /**
     * 删除所有子缓存
     * <p>使用SCAN遍历,KEYS为O(N)阻塞命令,生产环境禁止使用</p>
     */
    public void deleteAllChildren(KeyPrefix key) {
        Set<String> keys = this.scan(key.getPrefix() + ":*");
        if (CollUtil.isNotEmpty(keys)) {
            this.jsonRedisTemplate.delete(keys);
        }
    }

    /**
     * 删除所有缓存
     */
    public void deleteAll(KeyPrefix key) {
        this.jsonRedisTemplate.delete(key.getPrefix());
    }

    public void deleteValue(KeyPrefix keyPrefix, String key) {
        this.jsonRedisTemplate.delete(getRedisKey(keyPrefix, key));
    }

    /**
     * SCAN方式遍历匹配的Key
     *
     * @param pattern 匹配表达式
     * @return 匹配的Key集合
     */
    public Set<String> scan(String pattern) {
        return this.jsonRedisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = new HashSet<>();
            try (Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(pattern).count(500).build())) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            } catch (Exception e) {
                throw new BusinessException("Redis SCAN遍历失败:" + pattern + ":" + e.getMessage());
            }
            return keys;
        });
    }

    //==================================================================================================================

    public List<Object> valuesHashValue(KeyPrefix key) {
        return this.jsonRedisTemplate.opsForHash().values(key.getPrefix());
    }

    public List<Object> multiGetHashValue(KeyPrefix key, Collection<Object> hashKeys) {
        return this.jsonRedisTemplate.opsForHash().multiGet(key.getPrefix(), hashKeys);
    }

    public <T> void putAllHashValue(KeyPrefix key, Map<String, T> map) {
        this.jsonRedisTemplate.opsForHash().putAll(key.getPrefix(), map);
        this.expireHashKey(key);
    }

    public Object getHashValue(KeyPrefix key, String hashKey) {
        return this.jsonRedisTemplate.opsForHash().get(key.getPrefix(), hashKey);
    }

    public void putHashValue(KeyPrefix key, String hashKey, Object value) {
        this.jsonRedisTemplate.opsForHash().put(key.getPrefix(), hashKey, value);
        this.expireHashKey(key);
    }

    public void deleteHashValue(KeyPrefix key, Object[] ids) {
        this.jsonRedisTemplate.opsForHash().delete(key.getPrefix(), ids);
    }

    //==================================================================================================================

    public Object getValue(KeyPrefix keyPrefix, String key) {
        String redisKey = getRedisKey(keyPrefix, key);
        return this.jsonRedisTemplate.opsForValue().get(redisKey);
    }

    public void setValue(KeyPrefix keyPrefix, String key, Object value) {
        String redisKey = getRedisKey(keyPrefix, key);
        if (keyPrefix.expireSeconds() > 0) {
            this.jsonRedisTemplate.opsForValue().set(redisKey, value, keyPrefix.expireSeconds(), TimeUnit.SECONDS);
        } else {
            this.jsonRedisTemplate.opsForValue().set(redisKey, value);
        }
    }

    public Long increment(KeyPrefix keyPrefix, String key, long value) {
        String redisKey = getRedisKey(keyPrefix, key);

        return this.jsonRedisTemplate.opsForValue().increment(redisKey, value);
    }

    //==================================================================================================================

    public boolean hasKey(KeyPrefix keyPrefix, String key) {
        String redisKey = getRedisKey(keyPrefix, key);
        return jsonRedisTemplate.hasKey(redisKey);
    }

    public void expire(KeyPrefix keyPrefix) {
        Long ttl = getExpire(keyPrefix);
        if (ttl < 0 && keyPrefix.expireSeconds() > 0) {
            this.jsonRedisTemplate.expire(keyPrefix.getPrefix(), keyPrefix.expireSeconds(), TimeUnit.SECONDS);
        }
    }

    public Long getExpire(KeyPrefix keyPrefix) {
        return this.jsonRedisTemplate.getExpire(keyPrefix.getPrefix(), TimeUnit.SECONDS);
    }

    private String getRedisKey(KeyPrefix keyPrefix, String key) {
        return keyPrefix.getPrefix() + ":" + key;
    }

    /**
     * hash结构无法对单个field设置过期,对整个key设置过期时间(每次写入续期)
     * <p>否则hash缓存永不过期,只能靠写后删除,一旦删除失败脏数据永久驻留</p>
     */
    private void expireHashKey(KeyPrefix key) {
        if (key.expireSeconds() > 0) {
            this.jsonRedisTemplate.expire(key.getPrefix(), key.expireSeconds(), TimeUnit.SECONDS);
        }
    }

}
