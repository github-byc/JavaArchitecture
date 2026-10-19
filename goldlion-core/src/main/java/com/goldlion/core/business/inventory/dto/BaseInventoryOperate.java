package com.goldlion.core.business.inventory.dto;

import java.util.Collection;

/**
 * 基础库存操作
 *
 * @param <IOI> 基础库存操作指令
 */
public interface BaseInventoryOperate<IOI extends BaseInventoryOperateInstruction> {

    Collection<IOI> getInstructions();

}
