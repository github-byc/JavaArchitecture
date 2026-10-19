package com.goldlion.core.framework.deadletter.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * 死信日志
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "dead_letter_log", description = "死信日志")
public class DeadLetterLog {
    private String id;
    private Date createdOn;

    private String reason;
    private Long count;
    private Date time;
    private String exchange;
    private String scene;
    private String queue;
    private String routingKey;
    private String message;
}
