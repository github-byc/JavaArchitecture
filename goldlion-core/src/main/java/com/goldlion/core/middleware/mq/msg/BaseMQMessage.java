package com.goldlion.core.middleware.mq.msg;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 基础MQ消息
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseMQMessage implements MQMessage {
    private String id;
    private String operateUserName;
}
