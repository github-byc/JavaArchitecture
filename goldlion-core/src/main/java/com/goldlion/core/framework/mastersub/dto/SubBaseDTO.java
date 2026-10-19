package com.goldlion.core.framework.mastersub.dto;

import com.goldlion.core.framework.crud.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 主子表-子表DTO
 */
@Getter
@Setter
public class SubBaseDTO extends BaseDTO {

    @NotBlank(message = "主表ID不能为空")
    @Schema(description = "主表ID")
    private String masterId;

}
