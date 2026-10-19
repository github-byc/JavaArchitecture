package com.goldlion.core.business.inventory.rediskey;

import com.goldlion.core.middleware.redis.key.BaseKeyPrefix;

/**
 * 库存操作数据缓存键
 */
public class InventoryOperateDataCacheKey extends BaseKeyPrefix {

    public InventoryOperateDataCacheKey(String prefix) {
        super(prefix);
    }

    @Override
    protected String getSubPrefix() {
        return "INVENTORY_OPERATE_DATA_CACHE";
    }

    //==================================================================================================================

    public static final InventoryOperateDataCacheKey UNIQUE_KEY_ID = new InventoryOperateDataCacheKey("UniqueKeyId");

}
