package com.goldlion.core.business.order.dto;

import java.util.List;

/**
 * 基础订单操作
 *
 * @param <Order> 订单信息
 * @param <Item>  订单明细
 */
public interface BaseOrderOperate<Order extends BaseOrderInfo<?>, Item extends BaseOrderItem<?>> {

    /**
     * 订单信息
     */
    Order getOrderInfo();

    /**
     * 订单明细
     */
    List<Item> getOrderItems();

}
