package com.goldlion.core.framework.crud.rediskey;

import com.goldlion.core.middleware.redis.key.BaseKeyPrefix;

/**
 * 基础数据缓存键
 */
public class BaseDataCacheKey extends BaseKeyPrefix {

    private BaseDataCacheKey(Class<?> clazz) {
        super(clazz.getSimpleName());
    }

    private BaseDataCacheKey(Class<?> clazz, String prefix) {
        super(clazz.getSimpleName() + ":" + prefix, DEFAULT_EXPIRE);
    }

    private BaseDataCacheKey(Class<?> clazz, long expireSeconds) {
        super(clazz.getSimpleName(), expireSeconds);
    }

    @Override
    protected String getSubPrefix() {
        return "BASE_DATA_CACHE";
    }

    //==================================================================================================================

    public static BaseDataCacheKey page(Class<?> clazz) {
        return new BaseDataCacheKey(clazz, "PAGE");
    }

    public static BaseDataCacheKey list(Class<?> clazz) {
        return new BaseDataCacheKey(clazz, "LIST");
    }

    public static BaseDataCacheKey one(Class<?> clazz) {
        return new BaseDataCacheKey(clazz, "ONE");
    }

    public static BaseDataCacheKey baseKey(Class<?> clazz) {
        // id维度缓存同样设置过期时间(写后删除失败或事务窗口回填旧值时可自愈),前缀保持不变
        return new BaseDataCacheKey(clazz, DEFAULT_EXPIRE);
    }
}
