package com.goldlion.core.framework.crud.vo;

import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 基础VO
 */
@Getter
@Setter
public class BaseVO {

    @ExcelProperty(value = "主键ID")
    @Schema(description = "主键ID")
    private String id;

    @ExcelProperty(value = "乐观锁")
    @Schema(description = "乐观锁")
    private Integer version;

    @ExcelProperty(value = "创建者")
    @Schema(description = "创建者")
    private String createdBy;

    @ExcelProperty(value = "创建日期")
    @Schema(description = "创建日期")
    private LocalDateTime createdOn;

    @ExcelProperty(value = "修改者")
    @Schema(description = "修改者")
    private String modifiedBy;

    @ExcelProperty(value = "修改日期")
    @Schema(description = "修改日期")
    private LocalDateTime modifiedOn;
}
