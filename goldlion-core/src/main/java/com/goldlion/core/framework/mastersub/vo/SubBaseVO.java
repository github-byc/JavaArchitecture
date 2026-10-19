package com.goldlion.core.framework.mastersub.vo;

import cn.idev.excel.annotation.ExcelProperty;
import com.goldlion.core.framework.crud.vo.BaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 主子表-子表VO
 */
@Getter
@Setter
public class SubBaseVO extends BaseVO {

    @ExcelProperty(value = "主表ID")
    @Schema(description = "主表ID")
    private String masterId;

}
