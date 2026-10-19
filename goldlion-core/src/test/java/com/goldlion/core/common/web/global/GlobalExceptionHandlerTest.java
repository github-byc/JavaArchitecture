package com.goldlion.core.common.web.global;

import com.goldlion.core.common.domain.Result;
import com.goldlion.core.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 全局异常处理器测试:异常信息不外泄 + 友好映射
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/test");

    @Test
    @DisplayName("BusinessException:400 + 原始业务消息")
    void business() {
        ResponseEntity<Object> resp = handler.handleBusinessException(new BusinessException("库存不足"), request);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Result body = (Result) resp.getBody();
        assertThat(body.get(Result.MSG_TAG)).isEqualTo("库存不足");
    }

    @Test
    @DisplayName("RuntimeException:500 + 兜底文案,原始异常消息不回传")
    void runtimeMasked() {
        ResponseEntity<Object> resp = handler.handleRuntimeException(
                new RuntimeException("SELECT * FROM secret_table WHERE pwd=?"), request);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        Result body = (Result) resp.getBody();
        assertThat(body.get(Result.MSG_TAG)).isEqualTo(GlobalExceptionHandler.SYSTEM_ERROR_MSG);
        assertThat(body.get(Result.MSG_TAG).toString()).doesNotContain("secret_table");
    }

    @Test
    @DisplayName("Exception:500 + 兜底文案")
    void exceptionMasked() {
        ResponseEntity<Object> resp = handler.handleException(new Exception("/opt/app/internal path leak"), request);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(((Result) resp.getBody()).get(Result.MSG_TAG))
                .isEqualTo(GlobalExceptionHandler.SYSTEM_ERROR_MSG);
    }

    @Test
    @DisplayName("唯一键冲突:400 + 友好提示(不回传SQL细节)")
    void duplicateKey() {
        ResponseEntity<Object> resp = handler.handleDuplicateKeyException(
                new DuplicateKeyException("Duplicate entry 'x' for key 'uk_name'"), request);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        String msg = ((Result) resp.getBody()).get(Result.MSG_TAG).toString();
        assertThat(msg).contains("唯一键冲突");
        assertThat(msg).doesNotContain("uk_name");
    }

}
