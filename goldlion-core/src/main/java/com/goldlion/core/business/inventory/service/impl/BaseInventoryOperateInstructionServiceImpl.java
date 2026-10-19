package com.goldlion.core.business.inventory.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import com.goldlion.core.business.inventory.dto.BaseInventoryAccount;
import com.goldlion.core.business.inventory.dto.BaseInventoryOperate;
import com.goldlion.core.business.inventory.dto.BaseInventoryOperateInstruction;
import com.goldlion.core.business.inventory.rediskey.InventoryOperateDataCacheKey;
import com.goldlion.core.business.inventory.service.BaseInventoryOperateInstructionService;
import com.goldlion.core.common.exception.BusinessException;
import com.goldlion.core.common.function.Tuple3;
import com.goldlion.core.middleware.redis.RedisOperator;
import jakarta.annotation.Resource;
import org.springframework.aop.framework.AopContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 基本库存操作指令 服务实现类
 *
 * @param <PK>  主键类型
 * @param <IA>  库存账户对象
 * @param <IO>  库存操作对象
 * @param <IOI> 库存操作指令对象
 */
public abstract class BaseInventoryOperateInstructionServiceImpl<PK, IA extends BaseInventoryAccount<PK>, IO extends BaseInventoryOperate<IOI>, IOI extends BaseInventoryOperateInstruction>
        implements BaseInventoryOperateInstructionService<IO> {

    @Resource
    protected RedisOperator redisOperator;

    //==================================================================================================================

    @Override
    public IO execute(IO inventoryOperate) {
        StaticLog.info("执行库存操作指令");

        Collection<IOI> inventoryOperateInstructions = inventoryOperate.getInstructions();

        // 1.校验库存操作指令
        this.verifyInventoryOperateInstruction(inventoryOperateInstructions);

        // 2.汇总库存操作指令
        StaticLog.info("汇总前的size: {}", inventoryOperateInstructions.size());
        Collection<IOI> summaryInventoryOperateInstructions = this.summaryInventoryOperateInstruction(inventoryOperateInstructions);
        StaticLog.info("汇总后的size: {}", summaryInventoryOperateInstructions.size());

        // 3.查询库存账户数据
        Collection<IA> inventoryAccounts = this.searchInventoryAccount(summaryInventoryOperateInstructions);
        StaticLog.info("查询库存账户数据: {}", inventoryAccounts.size());

        // 4.校验库存账户数据
        Tuple3<Collection<IOI>, Collection<IOI>, Map<PK, IOI>> saveOrUpdateData = this.verifyInventoryAccount(inventoryAccounts, summaryInventoryOperateInstructions);

        // 5.保存或更新数据
        getCurrentProxy().saveOrUpdateData(inventoryOperate, saveOrUpdateData);

        return inventoryOperate;
    }

    /**
     * 校验库存操作指令
     */
    public void verifyInventoryOperateInstruction(Collection<IOI> inventoryOperateInstructions) {
        if (inventoryOperateInstructions.stream().anyMatch(t -> StrUtil.isBlank(t.getInventoryAccountUniqueKeyToStr()))) {
            throw new BusinessException("库存账户唯一键不能为空!");
        }
    }

    /**
     * 汇总库存操作指令
     */
    public Collection<IOI> summaryInventoryOperateInstruction(Collection<IOI> inventoryOperateInstructions) {
        // 汇总变动数量
        Map<String, IOI> uniqueKeyInventoryAccountMap = inventoryOperateInstructions.stream()
                .collect(Collectors.toMap(
                        IOI::getInventoryAccountUniqueKeyToStr,
                        Function.identity(),
                        (v1, v2) -> {
                            v1.setChangeQty(v1.getChangeQty() + v2.getChangeQty());
                            return v1;
                        }));

        return uniqueKeyInventoryAccountMap.values();
    }

    /**
     * 查询库存账户
     */
    public Collection<IA> searchInventoryAccount(Collection<IOI> summaryInventoryOperateInstructions) {
        // 3.1 获取库存账户唯一键
        Set<String> uniqueKeys = summaryInventoryOperateInstructions.stream().map(BaseInventoryOperateInstruction::getInventoryAccountUniqueKeyToStr).collect(Collectors.toUnmodifiableSet());

        // 3.2 用库存账户唯一键从缓存中获取库存账户id
        Set<String> inventoryAccountIds = getRedisCache(uniqueKeys);

        // 3.3 用库存账户id查询对应的库存账户
        Collection<IA> inventoryAccountByIds;
        if (CollUtil.isNotEmpty(inventoryAccountIds)) {
            inventoryAccountByIds = this.searchInventoryAccountByIds(inventoryAccountIds);
        } else {
            inventoryAccountByIds = Collections.emptySet();
        }

        // 3.4 找出未查询到库存账户的数据
        // 在缓存中的唯一键
        Set<String> uniqueKeyInCache = inventoryAccountByIds.stream().map(BaseInventoryAccount::getInventoryAccountUniqueKeyToStr).collect(Collectors.toUnmodifiableSet());
        // 不在缓存中的数据
        List<IOI> dataNotInCache = summaryInventoryOperateInstructions.stream().filter(t -> !uniqueKeyInCache.contains(t.getInventoryAccountUniqueKeyToStr())).toList();

        // 3.5 用不在缓存中的数据,根据具体的业务逻辑查询库存账户
        Collection<IA> inventoryAccountByDbs;
        if (CollUtil.isNotEmpty(dataNotInCache)) {
            inventoryAccountByDbs = this.searchInventoryAccountByData(dataNotInCache);
        } else {
            inventoryAccountByDbs = Collections.emptySet();
        }

        // 3.6 把库存账户id放入缓存中
        if (CollUtil.isNotEmpty(inventoryAccountByDbs)) {
            setRedisCache(inventoryAccountByDbs);
        }

        // 3.7 合并数据并返回
        return CollUtil.union(inventoryAccountByIds, inventoryAccountByDbs);
    }

    /**
     * 根据ID查询库存账户
     *
     * @param inventoryAccountIds 库存账户id
     * @return 库存账户
     */
    protected abstract Collection<IA> searchInventoryAccountByIds(Set<String> inventoryAccountIds);

    /**
     * 根据数据查询库存账户
     *
     * @param dataNotInCache 数据
     * @return 库存账户
     */
    protected abstract Collection<IA> searchInventoryAccountByData(Collection<IOI> dataNotInCache);

    /**
     * 校验库存账户并获取保存或更新库存数据
     *
     * @return 保存或更新库存数据<校验失败的数据, 需要创建的数据, 需要更新的数据>
     */
    public Tuple3<Collection<IOI>, Collection<IOI>, Map<PK, IOI>> verifyInventoryAccount(Collection<IA> inventoryAccounts, Collection<IOI> summaryInventoryOperateInstructions) {
        // 需要创建的数据
        List<IOI> needCreateData = CollUtil.newArrayList();
        // 需要更新的数据
        Map<PK, IOI> needUpdateData = MapUtil.newHashMap();
        // 校验失败的数据
        List<IOI> checkFailedData = CollUtil.newArrayList();

        // 根据库存账户唯一键分组
        Map<String, IA> inventoryAccountMap = inventoryAccounts.stream().collect(Collectors.toMap(BaseInventoryAccount::getInventoryAccountUniqueKeyToStr, Function.identity()));

        for (IOI summaryInventoryOperateInstruction : summaryInventoryOperateInstructions) {
            String uniqueKey = summaryInventoryOperateInstruction.getInventoryAccountUniqueKeyToStr();
            // 判断库存操作指令是否有对应的库存账户
            if (inventoryAccountMap.containsKey(uniqueKey)) {
                IA inventoryAccount = inventoryAccountMap.get(uniqueKey);

                // 校验库存账户和库存操作指令
                if (this.verifyInventoryAccountAndInstruction(inventoryAccount, summaryInventoryOperateInstruction)) {
                    // 通过,添加需要更新的数据
                    needUpdateData.put(inventoryAccount.getId(), summaryInventoryOperateInstruction);
                } else {
                    // 未通过,添加校验失败的数据
                    checkFailedData.add(summaryInventoryOperateInstruction);
                }
            } else {
                // 没有,添加需要创建的数据
                needCreateData.add(summaryInventoryOperateInstruction);
            }
        }

        return new Tuple3<>(checkFailedData, needCreateData, needUpdateData);
    }

    /**
     * 校验库存账户和库存操作指令
     */
    protected abstract boolean verifyInventoryAccountAndInstruction(IA inventoryAccount, IOI inventoryOperateInstruction);

    //==================================================================================================================

    /**
     * 保存或更新数据
     *
     * @param saveOrUpdateData 保存或更新数据<校验失败的数据, 需要创建的数据, 需要更新的数据>
     */
    @Transactional
    public void saveOrUpdateData(IO inventoryOperate, Tuple3<Collection<IOI>, Collection<IOI>, Map<PK, IOI>> saveOrUpdateData) {
        // 5.1 处理失败的数据
        Collection<IOI> checkFailedData = saveOrUpdateData.getT1();
        StaticLog.info("处理失败的数据: {}", checkFailedData.size());
        this.processingFailedData(inventoryOperate, checkFailedData);

        // 5.2 处理创建的数据
        Collection<IOI> needCreateData = saveOrUpdateData.getT2();
        StaticLog.info("处理创建的数据: {}", needCreateData.size());
        Collection<IA> createdData = this.processingCreatedData(inventoryOperate, needCreateData);

        // 5.3 把创建的库存账户id放入缓存中
        if (CollUtil.isNotEmpty(createdData)) {
            StaticLog.info("创建的数据放入缓存: {}", createdData.size());
            setRedisCache(createdData);
        }

        // 5.4 处理更新的数据
        Map<PK, IOI> needUpdateData = saveOrUpdateData.getT3();
        StaticLog.info("处理更新的数据: {}", needUpdateData.size());
        this.processingUpdatedData(inventoryOperate, needUpdateData);

        // 5.5 处理流水数据
        Collection<IOI> flowData = CollUtil.union(needCreateData, needUpdateData.values());
        this.processingFlowData(inventoryOperate, flowData);
    }


    /**
     * 处理失败的数据
     *
     * @param checkFailedData 校验失败的数据
     */
    protected abstract void processingFailedData(IO inventoryOperate, Collection<IOI> checkFailedData);

    /**
     * 处理创建的数据
     *
     * @param needCreateData 需要创建的数据
     */
    protected abstract Collection<IA> processingCreatedData(IO inventoryOperate, Collection<IOI> needCreateData);

    /**
     * 处理更新的数据
     *
     * @param needUpdateData 需要更新的数据
     */
    protected abstract void processingUpdatedData(IO inventoryOperate, Map<PK, IOI> needUpdateData);

    /**
     * 处理流水数据
     */
    protected abstract void processingFlowData(IO inventoryOperate, Collection<IOI> flowData);

    //==================================================================================================================

    private Set<String> getRedisCache(Set<String> uniqueKeys) {
        List<Object> objects = this.redisOperator.multiGetHashValue(InventoryOperateDataCacheKey.UNIQUE_KEY_ID, Arrays.asList(uniqueKeys.toArray()));
        return objects.stream().filter(Objects::nonNull).map(Convert::toStr).collect(Collectors.toUnmodifiableSet());
    }

    private void setRedisCache(Collection<IA> inventoryAccountByDbs) {
        Map<String, PK> dataCache = inventoryAccountByDbs.stream().collect(Collectors.toMap(BaseInventoryAccount::getInventoryAccountUniqueKeyToStr, BaseInventoryAccount::getId));
        this.redisOperator.putAllHashValue(InventoryOperateDataCacheKey.UNIQUE_KEY_ID, dataCache);
    }

    private BaseInventoryOperateInstructionServiceImpl<PK, IA, IO, IOI> getCurrentProxy() {
        return (BaseInventoryOperateInstructionServiceImpl<PK, IA, IO, IOI>) AopContext.currentProxy();
    }

}
