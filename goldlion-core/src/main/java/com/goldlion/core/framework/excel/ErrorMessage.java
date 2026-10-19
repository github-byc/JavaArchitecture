package com.goldlion.core.framework.excel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * 校验错误信息
 * </p>
 *
 * @author mao.lei
 * @since 2025-04-15
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorMessage {

    @Schema(description = "行号")
    private Long lineNum;

    @Schema(description = "错误信息")
    private Set<String> errors = new HashSet<>();

}
