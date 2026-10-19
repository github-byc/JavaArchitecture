package com.goldlion.core.middleware.redis.key;

/**
 * redis Key
 */
public interface KeyPrefix {

    /**
     * 过期秒
     */
    long expireSeconds();

    /**
     * 前缀
     */
    String getPrefix();

}
