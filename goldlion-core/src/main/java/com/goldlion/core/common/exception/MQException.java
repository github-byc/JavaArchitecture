package com.goldlion.core.common.exception;

import lombok.Getter;

@Getter
public class MQException extends BaseException {

    private Object data;

    public MQException(String message) {
        super(message);
    }

    public MQException(String message, Throwable cause) {
        super(message, cause);
    }

    public MQException(String message, Throwable cause, Object data) {
        super(message, cause);
        this.data = data;
    }

}
