package com.goldlion.core.business.order.dto;

import com.goldlion.core.business.inventory.dto.BaseInventoryAccountUniqueKey;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 基础订单明细
 *
 * @param <PK> 主键类型
 */
public interface BaseOrderItem<PK> extends BaseInventoryAccountUniqueKey {

    @Schema(hidden = true)
    PK getId();

}
