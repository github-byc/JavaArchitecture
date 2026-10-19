package com.goldlion.core.framework.mastersub.vo;

import cn.idev.excel.annotation.ExcelProperty;
import com.goldlion.core.framework.crud.vo.BaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 主子表-主表VO
 */
@Getter
@Setter
public class MasterBaseVO extends BaseVO {

    @ExcelProperty(value = "单据编号")
    @Schema(description = "单据编号")
    private String orderNum;

    @ExcelProperty(value = "单据状态")
    @Schema(description = "单据状态")
    private String orderStatus;

}
