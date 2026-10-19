package com.goldlion.core.framework.excel.dto;

import cn.idev.excel.annotation.ExcelIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Excel上传DTO
 */
@Getter
@Setter
public class ExcelUploadDTO {

    @ExcelIgnore
    @Schema(description = "行号")
    private Long lineNum;
}
