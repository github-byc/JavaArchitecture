package com.goldlion.core.common.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class BusinessException extends BaseException {

    private List<Object> datas = null;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, List<Object> datas) {
        super(message);
        this.datas = datas;
    }

}
