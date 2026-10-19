package com.goldlion.core.framework.security.domain.rediskey;

import com.goldlion.core.middleware.redis.key.BaseKeyPrefix;

/**
 * 登录数据缓存键
 */
public class LoginDataCacheKey extends BaseKeyPrefix {

    private LoginDataCacheKey(String prefix) {
        super(prefix, DEFAULT_EXPIRE);
    }

    @Override
    protected String getSubPrefix() {
        return "LOGIN_DATA_CACHE";
    }

    //==================================================================================================================

    public static final LoginDataCacheKey LOGIN_TOKEN = new LoginDataCacheKey("LOGIN_TOKEN");
    public static final LoginDataCacheKey OAUTH2_LOGIN_TOKEN = new LoginDataCacheKey("OAUTH2_LOGIN_TOKEN");

}
