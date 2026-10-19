package com.goldlion.core.middleware.mq.enums;

import cn.hutool.core.text.StrPool;
import com.goldlion.core.middleware.mq.MQUsageScene;
import lombok.Getter;

/**
 * MQ交换机枚举
 */
@Getter
public enum MQExchangeEnum {
    DEAD_LETTER_EXCHANGE("死信交换机", "DLX"),
    DIRECT("默认直接交换机", "direct");

    private final String desc;
    private final String exchange;

    MQExchangeEnum(String desc, String exchange) {
        this.desc = MQUsageScene.PREFIX.get() + StrPool.DOT + desc;
        this.exchange = MQUsageScene.PREFIX.get() + StrPool.DOT + exchange;
    }
}
