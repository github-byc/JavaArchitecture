package com.goldlion.core.framework.mastersub.web;

import com.goldlion.core.common.domain.dto.BaseIdDTO;
import com.goldlion.core.framework.excel.web.ExcelController;
import com.goldlion.core.framework.mastersub.dto.*;
import com.goldlion.core.framework.mastersub.entity.MasterBaseEntity;
import com.goldlion.core.framework.mastersub.entity.OrderBaseEntity;
import com.goldlion.core.framework.mastersub.service.MasterBaseService;
import com.goldlion.core.framework.mastersub.service.OrderBaseService;
import com.goldlion.core.framework.mastersub.vo.MasterBaseVO;
import com.goldlion.core.framework.mastersub.vo.OrderBaseVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 单据主子表-主表控制器
 */
public class OrderExcelController<
        BS extends OrderBaseService<? extends OrderBaseEntity, BDTO, EQDTO>,
        BDTO extends OrderBaseDTO,
        EQDTO extends OrderExcelQueryDTO,
        VO extends OrderBaseVO,
        ExcelUpload extends OrderExcelUploadDTO> extends ExcelController<BS, BDTO, EQDTO, VO, ExcelUpload> {

    @Operation(summary = "提交")
    @PostMapping(value = "/submit")
    public void submit(@RequestBody @Validated BaseIdDTO dto) {
        this.baseService.submit(dto.getId());
    }

    @Operation(summary = "审核")
    @PostMapping(value = "/audit")
    public void audit(@RequestBody @Validated BaseIdDTO dto) {
        this.baseService.audit(dto.getId());
    }

    @Operation(summary = "作废")
    @PostMapping(value = "/invalid")
    public void invalid(@RequestBody @Validated BaseIdDTO dto) {
        this.baseService.invalid(dto.getId());
    }

}
