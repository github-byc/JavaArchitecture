package com.goldlion.core.common.exception;

import lombok.Getter;

@Getter
public class ExcelException extends BaseException {

    private Object data;

    public ExcelException(String message) {
        super(message);
    }

    public ExcelException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExcelException(String message, Object data) {
        super(message);
        this.data = data;
    }

}
