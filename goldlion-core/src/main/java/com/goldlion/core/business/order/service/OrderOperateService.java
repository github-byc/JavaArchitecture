package com.goldlion.core.business.order.service;

import com.goldlion.core.business.order.dto.BaseOrderOperate;
import com.goldlion.core.common.domain.Result;

/**
 * 订单操作 服务类
 */
public interface OrderOperateService<OP extends BaseOrderOperate<?, ?>> {

    /**
     * 创建订单
     */
    Result createOrder(OP orderOperate);

}
