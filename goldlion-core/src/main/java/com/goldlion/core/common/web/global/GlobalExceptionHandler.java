package com.goldlion.core.common.web.global;

import cn.hutool.log.StaticLog;
import com.goldlion.core.common.constant.GlobalLogInfo;
import com.goldlion.core.common.domain.Result;
import com.goldlion.core.common.exception.BusinessException;
import com.goldlion.core.common.exception.ExcelException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
public class GlobalExceptionHandler {

    /**
     * 系统异常兜底文案,真实异常信息只进日志不回传前端,防止泄露SQL、内部路径等敏感信息
     */
    public static final String SYSTEM_ERROR_MSG = "系统繁忙,请稍后重试";

    //==================================================================================================================

    /**
     * BindException异常处理
     * <p>
     * 当BindingResult中存在错误信息时，会抛出BindException异常。
     */
    @ExceptionHandler({BindException.class})
    public ResponseEntity<Object> handleBindException(BindException ex, HttpServletRequest request) {
        String errorMsg = ex.getBindingResult().getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(";"));
        return this.createResponseEntity(ex, request, errorMsg, null, HttpStatus.BAD_REQUEST);
    }

    /**
     * MethodArgumentNotValidException异常处理
     * <p>
     * MethodArgumentNotValidException异常校验 @RequestBody标注的JSON对象参数
     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errorMsg = ex.getBindingResult().getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(";"));
        return this.createResponseEntity(ex, request, errorMsg, null, ex.getStatusCode());
    }

    /**
     * ExcelException异常处理
     */
    @ExceptionHandler({ExcelException.class})
    public ResponseEntity<Object> handleExcelException(ExcelException ex, HttpServletRequest request) {
        return this.createResponseEntity(ex, request, ex.getMessage(), ex.getData(), HttpStatus.BAD_REQUEST);
    }

    /**
     * BusinessException异常处理
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        return this.createResponseEntity(ex, request, ex.getMessage(), ex.getDatas(), HttpStatus.BAD_REQUEST);
    }

    /**
     * DuplicateKeyException异常处理(唯一键冲突)
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Object> handleDuplicateKeyException(DuplicateKeyException ex, HttpServletRequest request) {
        return this.createResponseEntity(ex, request, "数据已存在(唯一键冲突),请检查后重试", null, HttpStatus.BAD_REQUEST);
    }

    /**
     * RuntimeException异常处理
     * <p>不回传原始异常消息,防止泄露内部信息,详见日志</p>
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        return this.createResponseEntity(ex, request, SYSTEM_ERROR_MSG, null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Exception异常处理
     * <p>不回传原始异常消息,防止泄露内部信息,详见日志</p>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex, HttpServletRequest request) {
        return this.createResponseEntity(ex, request, SYSTEM_ERROR_MSG, null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    //==================================================================================================================

    protected ResponseEntity<Object> createResponseEntity(Exception ex, HttpServletRequest request, String errorMsg, Object data, HttpStatusCode statusCode) {
        Result result = getResult(ex, request, errorMsg, data, statusCode);
        return ResponseEntity.status(statusCode).body(result);
    }

    private Result getResult(Exception ex, HttpServletRequest request, String errorMsg, Object data, HttpStatusCode statusCode) {
        StaticLog.error(ex, GlobalLogInfo.ERROR_INFO, request.getRequestURI(), ex.getClass().getSimpleName(), ex.getMessage());
        if (Objects.isNull(data)) {
            return Result.error(statusCode.value(), errorMsg);
        } else {
            return new Result(statusCode.value(), errorMsg, data);
        }
    }
}
