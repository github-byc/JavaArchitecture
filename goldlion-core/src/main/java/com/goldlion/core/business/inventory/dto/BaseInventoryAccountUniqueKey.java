package com.goldlion.core.business.inventory.dto;

import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.text.StrPool;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 基本库存账户唯一键
 */
public interface BaseInventoryAccountUniqueKey {

    /**
     * 获取库存账户唯一键
     */
    @Schema(hidden = true)
    default String getInventoryAccountUniqueKeyToStr() {
        StrBuilder builder = StrBuilder.create();
        this.getInventoryAccountUniqueKey().forEach(key -> builder.append(key).append(StrPool.UNDERLINE));
        return builder.toStringAndReset();
    }

    /**
     * 获取唯一键集合
     */
    @Schema(hidden = true)
    List<Object> getInventoryAccountUniqueKey();

}
