package com.goldlion.core.middleware.mq.producer;

import cn.hutool.json.JSONUtil;
import com.goldlion.core.framework.deadletter.model.DeadLetterLog;
import com.goldlion.core.middleware.mq.MQUsageScene;
import com.goldlion.core.middleware.mq.enums.MQExchangeEnum;
import com.goldlion.core.middleware.mq.msg.MQMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * RabbitMQ生产者实现类
 */
@Slf4j
@Component
public class RabbitProducer implements MQProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    //==================================================================================================================

    @Override
    public void sendMsg(MQUsageScene scene, MQMessage msg) {
        String jsonStr = JSONUtil.toJsonStr(msg);
        String routingKey = scene.getRoutingKey();
        log.info("发送MQ消息:RoutingKey={},Message={}", routingKey, jsonStr);

        Message message = MessageBuilder
                .withBody(jsonStr.getBytes(StandardCharsets.UTF_8))
                // 消息持久化,broker重启不丢(需配合durable队列,注解@Queue默认durable)
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .setHeader("scene", scene.getScene())
                .build();

        // 自动生成唯一ID
        CorrelationData correlationData = this.newCorrelationData(routingKey);
        rabbitTemplate.send(MQExchangeEnum.DIRECT.getExchange(), routingKey, message, correlationData);
    }

    @Override
    public void reDelivery(DeadLetterLog deadLetterLog) {
        log.info("重新投递MQ消息:{}", JSONUtil.toJsonStr(deadLetterLog));

        Message message = MessageBuilder
                .withBody(deadLetterLog.getMessage().getBytes(StandardCharsets.UTF_8))
                // 消息持久化,broker重启不丢(需配合durable队列,注解@Queue默认durable)
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .setHeader("scene", deadLetterLog.getScene())
                .build();

        // 自动生成唯一ID
        CorrelationData correlationData = this.newCorrelationData(deadLetterLog.getRoutingKey());
        rabbitTemplate.send(MQExchangeEnum.DIRECT.getExchange(), deadLetterLog.getRoutingKey(), message, correlationData);
    }

    //==================================================================================================================

    /**
     * 构建带发送结果回报的CorrelationData
     * <p>开启publisher confirm(spring.rabbitmq.publisher-confirm-type=correlated)后,
     * 发送异常或broker拒收会打error日志;未开启confirm时future不会回调,无副作用。
     * 持久化解决"到了不丢",confirm解决"到没到知道",二者配合才有完整交付保障</p>
     */
    private CorrelationData newCorrelationData(String routingKey) {
        CorrelationData correlationData = new CorrelationData();
        correlationData.getFuture().whenComplete((confirm, throwable) -> {
            if (throwable != null) {
                log.error("MQ消息发送异常:RoutingKey={}", routingKey, throwable);
            } else if (confirm != null && !confirm.isAck()) {
                log.error("MQ消息被broker拒收(nack):RoutingKey={},Reason={}", routingKey, confirm.getReason());
            }
        });
        return correlationData;
    }
}
