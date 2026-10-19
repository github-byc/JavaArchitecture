package com.goldlion.core.business.order.service.impl;

import com.goldlion.core.business.order.dto.BaseOrderInfo;
import com.goldlion.core.business.order.dto.BaseOrderItem;
import com.goldlion.core.business.order.dto.BaseOrderOperate;
import com.goldlion.core.business.order.service.OrderOperateService;
import com.goldlion.core.common.domain.Result;
import com.goldlion.core.common.exception.BusinessException;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订单操作 服务实现类
 */
public abstract class OrderOperateServiceImpl<
        PK,
        OP extends BaseOrderOperate<? extends BaseOrderInfo<PK>, ? extends BaseOrderItem<PK>>
        > implements OrderOperateService<OP> {

    @Transactional
    @Override
    public Result createOrder(OP orderOperate) {

        // 1.校验订单数据
        Result result = this.verifyOrderData(orderOperate);
        if (!result.isSuccess()) {
            return result;
        }

        // 2.减少订单库存
        boolean a = this.decreaseOrderInventory(orderOperate);
        if (!a) {
            throw new BusinessException("库存不足");
        }

        try {
            // 3.保存或更新数据订单
            this.saveOrUpdateData(orderOperate);
        } catch (Exception e) {
            // 4.处理保存或更新异常
            this.handleSaveOrUpdateExceptions(orderOperate);
            throw new BusinessException(e.getMessage());
        }

        return Result.success();
    }

    //==================================================================================================================

    /**
     * 校验订单数据
     *
     * @return 经过测试抛异常非常影响性能，这里校验订单的时候如果不通过，返回Result.error
     */
    protected abstract Result verifyOrderData(OP orderOperate);

    /**
     * 减少订单库存
     */
    public abstract boolean decreaseOrderInventory(OP orderOperate);

    /**
     * 保存或更新数据订单
     */
    protected abstract void saveOrUpdateData(OP orderOperate);

    /**
     * 处理保存或更新异常
     */
    protected void handleSaveOrUpdateExceptions(OP orderOperate) {
    }

}
