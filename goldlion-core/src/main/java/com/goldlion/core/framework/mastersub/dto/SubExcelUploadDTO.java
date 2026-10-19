package com.goldlion.core.framework.mastersub.dto;

import cn.idev.excel.annotation.ExcelProperty;
import com.goldlion.core.framework.excel.dto.ExcelUploadDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 主子表-子表Excel DTO
 */
@Getter
@Setter
public class SubExcelUploadDTO extends ExcelUploadDTO {

    @NotBlank(message = "主表ID不能为空")
    @ExcelProperty(value = "主表ID")
    private String masterId;

}
