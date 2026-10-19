package com.goldlion.core.middleware.mq.producer;

import com.goldlion.core.framework.deadletter.model.DeadLetterLog;
import com.goldlion.core.middleware.mq.MQUsageScene;
import com.goldlion.core.middleware.mq.msg.MQMessage;

/**
 * MQ生产者
 */
public interface MQProducer {

    /**
     * 发送MQ消息
     */
    void sendMsg(MQUsageScene scene, MQMessage msg);

    /**
     * 重新投递
     */
    void reDelivery(DeadLetterLog deadLetterLog);
}
