package com.goldlion.core.middleware.redis.key;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * redis Key 基础实现
 */
@Component
public abstract class BaseKeyPrefix implements KeyPrefix {
    /**
     * 分钟
     */
    protected static final int MINUTE = 60;
    /**
     * 小时
     */
    protected static final int HOUR = MINUTE * 60;
    /**
     * 天
     */
    protected static final int DAYS = HOUR * 24;

    /**
     * 默认8小时
     */
    protected static final int DEFAULT_EXPIRE = HOUR * 8;

    /**
     * 前缀
     */
    private final String prefix;
    /**
     * 过期秒数
     */
    private final long expireSeconds;

    public BaseKeyPrefix(String prefix) {
        //0代表永不过期
        this(prefix, 0L);
    }

    public BaseKeyPrefix(String prefix, long expireSeconds) {
        this.prefix = prefix;
        this.expireSeconds = expireSeconds;
    }

    //==================================================================================================================

    /**
     * 父前缀
     */
    private static final ThreadLocal<String> PARENT_PREFIX = ThreadLocal.withInitial(() ->
            Optional.ofNullable(SpringUtil.getProperty("goldlion.redis.parentPrefix"))
                    .filter(StrUtil::isNotBlank)
                    .orElseGet(() -> Optional.ofNullable(SpringUtil.getApplicationName())
                            .filter(StrUtil::isNotBlank)
                            .orElse("DEFAULT_REDIS_PARENT_PREFIX")));

    /**
     * 子前缀
     */
    protected abstract String getSubPrefix();

    //==================================================================================================================

    @Override
    public long expireSeconds() {//默认0代表永不过期
        return expireSeconds;
    }

    @Override
    public String getPrefix() {
        return PARENT_PREFIX.get() + ":" + this.getSubPrefix() + ":" + this.prefix;
    }

}
