package com.goldlion.core.business.inventory.dto;

/**
 * 基础库存操作指令
 */
public interface BaseInventoryOperateInstruction extends BaseInventoryAccountUniqueKey {

    /**
     * 获取变动数量
     */
    int getChangeQty();

    /**
     * 设置变动数量
     */
    void setChangeQty(int changeQty);

}
