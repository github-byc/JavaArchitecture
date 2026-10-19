package com.goldlion.core.framework.deadletter.rediskey;

import com.goldlion.core.middleware.redis.key.BaseKeyPrefix;

/**
 * 死信数据缓存键
 */
public class DeadLetterDataCacheKey extends BaseKeyPrefix {

    public DeadLetterDataCacheKey(String prefix) {
        super(prefix);
    }

    @Override
    protected String getSubPrefix() {
        return "DEAD_LETTER_DATA_CACHE";
    }

    public static final DeadLetterDataCacheKey LIST = new DeadLetterDataCacheKey("LIST");
}
