package com.goldlion.core.framework.mastersub.dto;

import com.goldlion.core.framework.crud.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 单据主子表-主表DTO
 */
@Getter
@Setter
public class OrderBaseDTO extends BaseDTO {

    @Schema(description = "单据编号")
    private String orderNum;

    @Schema(description = "单据状态")
    private String orderStatus;

}
