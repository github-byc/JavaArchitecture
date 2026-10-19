package com.goldlion.core.framework.mastersub.web;

import com.goldlion.core.common.domain.dto.BaseIdDTO;
import com.goldlion.core.common.validation.CreateGroup;
import com.goldlion.core.framework.crud.QueryBuilder;
import com.goldlion.core.framework.excel.web.ExcelController;
import com.goldlion.core.framework.mastersub.dto.SubBaseDTO;
import com.goldlion.core.framework.mastersub.dto.SubExcelQueryDTO;
import com.goldlion.core.framework.mastersub.dto.SubExcelUploadDTO;
import com.goldlion.core.framework.mastersub.entity.SubBaseEntity;
import com.goldlion.core.framework.mastersub.service.SubBaseService;
import com.goldlion.core.framework.mastersub.vo.SubBaseVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 主子表-子表控制器
 */
public class SubExcelController<
        BS extends SubBaseService<? extends SubBaseEntity, BDTO, EQDTO>,
        BDTO extends SubBaseDTO,
        EQDTO extends SubExcelQueryDTO,
        VO extends SubBaseVO,
        ExcelUpload extends SubExcelUploadDTO> extends ExcelController<BS, BDTO, EQDTO, VO, ExcelUpload> {

    @Operation(summary = "批量创建")
    @PostMapping(value = "/createBatch")
    public void createBatch(@RequestBody @Validated(value = CreateGroup.class) List<BDTO> dtos) {
        this.baseService.batchCreate(dtos);
    }

    @Operation(summary = "清空子表")
    @PostMapping(value = "/clear")
    public void clear(@RequestBody @Validated BaseIdDTO dto) {
        this.baseService.deleteByMasterId(dto.getId());
    }

    @Override
    protected void beforeSearch(EQDTO dto) {
        // 设置查询条件
        QueryBuilder.builder(dto)
                .eq(SubExcelQueryDTO::getMasterId, dto.getMasterId());
    }
}
