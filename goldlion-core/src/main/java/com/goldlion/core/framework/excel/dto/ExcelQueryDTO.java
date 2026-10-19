package com.goldlion.core.framework.excel.dto;

import com.goldlion.core.framework.crud.dto.QueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * Excel查询DTO
 */
@Getter
@Setter
public class ExcelQueryDTO extends QueryDTO {

    @Schema(description = "Excel条件")
    private ExcelCondition excelCondition;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExcelCondition {
        @Schema(description = "文件名")
        private String fileName;
        @Schema(description = "导出指定列")
        private Set<String> includeColumns;
        @Schema(description = "忽略指定列")
        private Set<String> excludeColumns;
    }
}
