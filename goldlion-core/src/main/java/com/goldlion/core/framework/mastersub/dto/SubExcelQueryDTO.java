package com.goldlion.core.framework.mastersub.dto;

import com.goldlion.core.framework.excel.dto.ExcelQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 主子表-子表查询DTO
 */
@Getter
@Setter
public class SubExcelQueryDTO extends ExcelQueryDTO {

    @NotBlank(message = "主表ID不能为空")
    @Schema(description = "主表ID")
    private String masterId;

}
