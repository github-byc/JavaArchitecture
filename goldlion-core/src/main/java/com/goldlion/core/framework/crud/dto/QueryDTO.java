package com.goldlion.core.framework.crud.dto;

import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询DTO
 */
@Getter
@Setter
public class QueryDTO implements java.io.Serializable {

    /**
     * 用来包装查询的条件
     * key-要查询的字段
     * value-查询匹配的方式，数据来源于 com.baomidou.mybatisplus.core.enums.SqlKeyword
     */
    @Schema(description = "查询条件")
    private List<Condition> queryCondition = new ArrayList<>();

    @Schema(description = "排序字段名称")
    private String sortName;

    @Schema(description = "排序顺序")
    private String sortType = SqlKeyword.DESC.getSqlSegment();

    @Schema(description = "是否缓存")
    private boolean cache = true;

    //==================================================================================================================

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Condition {
        @Schema(description = "要查询的字段")
        private String columnName;
        @Schema(description = "查询的匹配方式")
        private String sqlKeyword;
        @Schema(description = "要查询的字段值")
        private Object value;
        @Schema(description = "要查询的字段值")
        private Object value2;
    }

}


