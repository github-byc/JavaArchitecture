package com.goldlion.core.middleware.mq;

import cn.hutool.extra.spring.SpringUtil;

/**
 * MQ使用场景
 */
public interface MQUsageScene {
    /**
     * 前缀
     */
    ThreadLocal<String> PREFIX = ThreadLocal.withInitial(() -> SpringUtil.getProperty("spring.application.name"));

    /**
     * 场景说明
     */
    String getScene();

    /**
     * 队列
     */
    String getQueue();

    /**
     * 路由键
     */
    String getRoutingKey();

}
