package com.goldlion.core.middleware.oss.rediskey;

import com.goldlion.core.middleware.oss.OssClientSingleton;
import com.goldlion.core.middleware.redis.key.BaseKeyPrefix;

/**
 * OSS数据缓存键
 */
public class OssDataCacheKey extends BaseKeyPrefix {

    public OssDataCacheKey(String prefix, long expireSeconds) {
        super(prefix, expireSeconds);
    }

    @Override
    protected String getSubPrefix() {
        return "OSS_DATA_CACHE";
    }

    //==================================================================================================================

    public static final OssDataCacheKey PRESIGNED_URL = new OssDataCacheKey("PRESIGNED_URL", (OssClientSingleton.getInstance().getExpiration() / 1000) - 10);

}
