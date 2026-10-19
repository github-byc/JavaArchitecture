package com.goldlion.core.business.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 基础订单信息
 *
 * @param <PK> 主键类型
 */
public interface BaseOrderInfo<PK> {

    @Schema(hidden = true)
    PK getId();

}
