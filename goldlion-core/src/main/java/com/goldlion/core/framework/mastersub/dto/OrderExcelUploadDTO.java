package com.goldlion.core.framework.mastersub.dto;

import cn.idev.excel.annotation.ExcelProperty;
import com.goldlion.core.framework.excel.dto.ExcelUploadDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 单据主子表-主表Excel DTO
 */
@Getter
@Setter
public class OrderExcelUploadDTO extends ExcelUploadDTO {

    @ExcelProperty(value = "单据编号")
    @Schema(description = "单据编号")
    private String orderNum;

    @ExcelProperty(value = "单据状态")
    @Schema(description = "单据状态")
    private String orderStatus;

}
