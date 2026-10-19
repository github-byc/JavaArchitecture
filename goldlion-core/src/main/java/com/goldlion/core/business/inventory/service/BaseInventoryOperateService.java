package com.goldlion.core.business.inventory.service;


import com.goldlion.core.business.inventory.dto.BaseInventoryOperate;

/**
 * 基本库存操作 服务类
 *
 * @param <IO> 库存操作对象
 */
public interface BaseInventoryOperateService<IO extends BaseInventoryOperate<?>> {

    /**
     * 执行库存操作
     */
    IO execute(IO inventoryOperate);

}
