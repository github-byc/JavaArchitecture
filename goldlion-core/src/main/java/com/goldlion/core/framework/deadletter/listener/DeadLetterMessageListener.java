package com.goldlion.core.framework.deadletter.listener;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.goldlion.core.framework.deadletter.model.DeadLetterLog;
import com.goldlion.core.framework.deadletter.service.DeadLetterLogService;
import com.goldlion.core.middleware.mq.enums.MQUsageSceneEnum;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 死信消息侦听器
 *
 * @see <a href="https://docs.spring.io/spring-amqp/reference/3.1/amqp/receiving-messages/async-annotation-driven.html">注释驱动的侦听器端点</a>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeadLetterMessageListener {
    private final DeadLetterLogService deadLetterLogService;

    //==================================================================================================================

    @RabbitListener(
            ackMode = "MANUAL",
            // 绑定队列
            bindings = @QueueBinding(
                    // 创建交换机
                    exchange = @Exchange(value = "#{T(com.goldlion.core.middleware.mq.enums.MQExchangeEnum).DEAD_LETTER_EXCHANGE.getExchange()}"),
                    // 创建队列
                    value = @Queue(value = "#{T(com.goldlion.core.middleware.mq.enums.MQUsageSceneEnum).DEAD_LETTER_QUEUE.getQueue()}"),
                    // 路由键
                    key = "#{T(com.goldlion.core.middleware.mq.enums.MQUsageSceneEnum).DEAD_LETTER_QUEUE.getRoutingKey()}"))
    public void onMessage(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("MQ场景:{}", MQUsageSceneEnum.DEAD_LETTER_QUEUE.getScene());

        try {
            MessageProperties messageProperties = message.getMessageProperties();
            List<Map<String, ?>> xDeathHeader = messageProperties.getXDeathHeader();

            if (xDeathHeader.size() == 1) {
                String msg = new String(message.getBody(), StandardCharsets.UTF_8);
                JSONObject jsonObject = JSONUtil.parseObj(msg);

                Map<String, ?> deathHeader = xDeathHeader.get(0);

                List<String> routingKeys = (List<String>) deathHeader.get("routing-keys");
                String jsonStr = JSONUtil.toJsonStr(deathHeader);
                String scene = messageProperties.getHeader("scene");

                DeadLetterLog deadLetterLog = JSONUtil.toBean(jsonStr, DeadLetterLog.class);
                deadLetterLog.setId(jsonObject.getStr("id"));
                deadLetterLog.setCreatedOn(new Date());
                deadLetterLog.setScene(scene);
                deadLetterLog.setRoutingKey(routingKeys.get(0));
                deadLetterLog.setMessage(msg);
                log.debug("记录死信日志:{}", JSONUtil.toJsonStr(deadLetterLog));

                this.deadLetterLogService.insert(deadLetterLog);

                // 成功确认（第二个参数为是否批量确认）
                channel.basicAck(tag, false);
            } else {
                log.error("xDeathHeader size not 1,data:{}", JSONUtil.toJsonStr(xDeathHeader));
                // 丢弃消息:重新入队不会累计x-death,会形成无限循环,消息体已含在日志中可人工重发
                channel.basicReject(tag, false);
            }
        } catch (Exception e) {
            log.error("死信消息异常,丢弃消息(消息体见下方,可人工重发):{}",
                    new String(message.getBody(), StandardCharsets.UTF_8), e);
            // 丢弃消息:重新入队会立即重复消费,依赖方(如数据库)不可用时将无限打转
            channel.basicReject(tag, false);
        }
        // channel由Spring AMQP监听容器托管,不要手动close,否则破坏容器channel复用且可能干扰未完成的ack
    }

}
