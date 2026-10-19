package com.goldlion.core.middleware.mq.msg;

import java.io.Serializable;

/**
 * MQ消息
 */
public interface MQMessage extends Serializable {
    String getId();

    String getOperateUserName();
}
