package com.goldlion.core.middleware.mq.enums;

import cn.hutool.core.text.StrPool;
import com.goldlion.core.middleware.mq.MQUsageScene;
import lombok.Getter;

/**
 * MQ使用场景枚举
 */
@Getter
public enum MQUsageSceneEnum implements MQUsageScene {
    DEAD_LETTER_QUEUE("死信", "DLQ", "DL.RK");

    private final String scene;
    private final String queue;
    private final String routingKey;

    MQUsageSceneEnum(String scene, String queue, String routingKey) {
        this.scene = scene;
        this.queue = PREFIX.get() + StrPool.DOT + "queue" + StrPool.DOT + queue;
        this.routingKey = routingKey;
    }
}
