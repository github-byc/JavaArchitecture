package com.goldlion.core.common.web.global;

import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器默认实现
 * <p>GlobalExceptionHandler基类本身不带@ControllerAdvice,由本类挂注解并经自动装配注册;
 * 业务模块需要定制时,声明自己的@RestControllerAdvice子类并排除本Bean即可</p>
 */
@RestControllerAdvice
public class DefaultGlobalExceptionHandler extends GlobalExceptionHandler {

}
