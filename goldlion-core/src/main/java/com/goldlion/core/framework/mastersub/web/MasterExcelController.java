package com.goldlion.core.framework.mastersub.web;

import com.goldlion.core.common.domain.dto.BaseIdDTO;
import com.goldlion.core.framework.excel.web.ExcelController;
import com.goldlion.core.framework.mastersub.dto.MasterBaseDTO;
import com.goldlion.core.framework.mastersub.dto.MasterExcelQueryDTO;
import com.goldlion.core.framework.mastersub.dto.MasterExcelUploadDTO;
import com.goldlion.core.framework.mastersub.entity.MasterBaseEntity;
import com.goldlion.core.framework.mastersub.service.MasterBaseService;
import com.goldlion.core.framework.mastersub.vo.MasterBaseVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 主子表-主表控制器
 */
public class MasterExcelController<
        BS extends MasterBaseService<? extends MasterBaseEntity, BDTO, EQDTO>,
        BDTO extends MasterBaseDTO,
        EQDTO extends MasterExcelQueryDTO,
        VO extends MasterBaseVO,
        ExcelUpload extends MasterExcelUploadDTO> extends ExcelController<BS, BDTO, EQDTO, VO, ExcelUpload> {

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
