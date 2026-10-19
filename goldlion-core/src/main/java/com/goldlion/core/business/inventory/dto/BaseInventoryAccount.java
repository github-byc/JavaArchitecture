package com.goldlion.core.business.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 基本库存账户
 *
 * @param <PK> 主键
 */
public interface BaseInventoryAccount<PK> extends BaseInventoryAccountUniqueKey {

    @Schema(hidden = true)
    PK getId();

}
