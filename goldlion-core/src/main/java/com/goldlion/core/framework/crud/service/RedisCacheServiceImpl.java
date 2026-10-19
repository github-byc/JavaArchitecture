package com.goldlion.core.framework.crud.service;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.StaticLog;
import com.goldlion.core.framework.crud.dao.BaseDao;
import com.goldlion.core.framework.crud.dto.BaseDTO;
import com.goldlion.core.framework.crud.dto.QueryDTO;
import com.goldlion.core.framework.crud.entity.BaseEntity;
import com.goldlion.core.framework.crud.rediskey.BaseDataCacheKey;
import com.goldlion.core.middleware.redis.RedisOperator;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Redis缓存 基础服务实现类
 *
 * @param <M>    Mapper
 * @param <T>    持久化对象
 * @param <BDTO> 数据DTO
 * @param <QDTO> 查询DTO
 */
public class RedisCacheServiceImpl<M extends BaseDao<T>, T extends BaseEntity, BDTO extends BaseDTO, QDTO extends QueryDTO>
        extends MybatisPlusServiceImpl<M, T, BDTO, QDTO> {
    @Resource
    private RedisOperator redisOperator;

    public void fullDto(BDTO dto) {

    }

    public void fullDto(List<BDTO> dto) {

    }

    //==================================================================================================================

    @Transactional
    @Override
    public String create(BDTO dto) {
        fullDto(dto);
        String id = super.create(dto);
        // 删除缓存
        this.delCache(new Object[]{id});
        return id;
    }

    @Transactional
    @Override
    public List<T> batchCreate(List<BDTO> dtos) {
        fullDto(dtos);
        List<T> ts = super.batchCreate(dtos);
        // 删除缓存
        Object[] ids = ts.stream().map(BaseEntity::getId).toArray();
        this.delCache(ids);
        return ts;
    }

    @Transactional
    @Override
    public void update(BDTO dto) {
        super.update(dto);
        // 删除缓存
        this.delCache(new Object[]{dto.getId()});
    }

    @Transactional
    @Override
    public void batchUpdate(List<BDTO> dtos) {
        super.batchUpdate(dtos);
        // 删除缓存
        Object[] ids = dtos.stream().map(BaseDTO::getId).toArray();
        this.delCache(ids);
    }

    @Transactional
    @Override
    public void delete(String id) {
        super.delete(id);
        // 删除缓存
        this.delCache(new Object[]{id});
    }

    @Transactional
    @Override
    public void deletes(Set<String> ids) {
        super.deletes(ids);
        // 删除缓存
        this.delCache(ArrayUtil.toArray(ids, String.class));
    }

    @Override
    public T getById(String id) {
        return this.getCacheData(
                        () -> this.getIdCache(id),
                        () -> super.getById(id),
                        t -> this.setIdCache(id, t))
                .getData();
    }

    @Override
    public List<T> getByIds(Set<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }

        // 读取缓存
        List<T> ts = this.getCacheData(
                        () -> this.getIdsCache(ids),
                        () -> super.getByIds(ids),
                        this::setIdsCache)
                .getData();
        if (ids.size() == ts.size()) {
            return ts;
        }

        // 如果只有部分缓存数据
        // 获取存在缓存的id
        Set<String> existsIds = ts.stream().map(BaseEntity::getId).collect(Collectors.toSet());
        // 获取不存在缓存的id
        Set<String> notExistsIds = ids.stream().filter(id -> !existsIds.contains(id)).collect(Collectors.toSet());
        // 根据不存在缓存的id，查询数据库
        List<T> notExists = super.getByIds(notExistsIds);
        // 如果数据不为空
        if (CollUtil.isNotEmpty(notExists)) {
            // 写入缓存
            this.setIdsCache(notExists);
            // 合并数据
            CollUtil.addAll(ts, notExists);
        }

        return ts;
    }

    //==================================================================================================================

    // 查询结果(search/getOne/分页)不再缓存:查询组合按MD5作key无限膨胀Redis容量,
    // 且任意一行变更需失效该实体全部查询缓存,成本高于收益;
    // 仅保留id维度的读穿缓存(见getById/getByIds),列表/分页查询直查数据库,性能靠索引保障

    //==================================================================================================================

    /**
     * 获取缓存数据
     */
    private <Cache> CacheData<Cache> getCacheData(Supplier<CacheData<Cache>> getCacheData, Supplier<Cache> getDbData, Consumer<Cache> setCacheData) {
        // 读取缓存
        CacheData<Cache> cacheData = getCacheData.get();
        if (Objects.nonNull(cacheData.getData())) {
            StaticLog.debug("读取Redis缓存:{}", cacheData.getKey());
            return cacheData;
        }

        // 如果数据为空，先查询数据库
        Cache dbData = getDbData.get();
        StaticLog.debug("读取数据库:{}", cacheData.getKey());
        // 再写入缓存(空数据不回写,避免不存在的数据反复穿透并回写缓存)
        if (Objects.nonNull(dbData)) {
            setCacheData.accept(dbData);
        }
        cacheData.setData(dbData);
        return cacheData;
    }

    //==================================================================================================================

    /**
     * 通过集合主键,读取集合缓存
     */
    public CacheData<List<T>> getIdsCache(Set<String> ids) {
        BaseDataCacheKey key = BaseDataCacheKey.baseKey(super.getEntityClass());
        String hashKey = JSONUtil.toJsonStr(ids);

        List<Object> objects = this.redisOperator.multiGetHashValue(key, Arrays.asList(ids.toArray()));
        List<Object> cacheDatas = objects.stream().filter(Objects::nonNull).toList();
        if (CollUtil.isEmpty(cacheDatas)) {
            return CacheData.build(key, hashKey);
        }

        List<T> ts = BeanUtil.copyToList(cacheDatas, super.getEntityClass());
        return CacheData.build(key, hashKey, ts);
    }

    /**
     * 通过集合主键,写入集合缓存
     */
    public void setIdsCache(Collection<T> ts) {
        Map<String, T> map = ts.stream().collect(Collectors.toMap(BaseEntity::getId, Function.identity()));
        this.redisOperator.putAllHashValue(BaseDataCacheKey.baseKey(super.getEntityClass()), map);
    }

    /**
     * 通过主键,读取一个缓存
     */
    public CacheData<T> getIdCache(String id) {
        BaseDataCacheKey key = BaseDataCacheKey.baseKey(super.getEntityClass());

        Object o = this.redisOperator.getHashValue(key, id);
        if (Objects.isNull(o)) {
            return CacheData.build(key, id);
        }

        T t = BeanUtil.toBean(o, super.getEntityClass());
        return CacheData.build(key, id, t);
    }

    /**
     * 通过主键,写入一个缓存
     */
    public void setIdCache(String id, T t) {
        this.redisOperator.putHashValue(BaseDataCacheKey.baseKey(super.getEntityClass()), id, t);
    }

    /**
     * 删除缓存
     * <p>存在事务时在事务提交后执行:事务提交前删除缓存,并发请求会读到未提交的旧值并回填,
     * 脏缓存将驻留到过期;提交后删除则无此窗口</p>
     */
    public void delCache(Object[] ids) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    doDelCache(ids);
                }
            });
        } else {
            doDelCache(ids);
        }
    }

    private void doDelCache(Object[] ids) {
        BaseDataCacheKey key = BaseDataCacheKey.baseKey(super.getEntityClass());
        StaticLog.info("删除Redis缓存:{},{}", key.getPrefix(), ids);
        this.redisOperator.deleteHashValue(key, ids);
        this.redisOperator.deleteAllChildren(key);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheData<Cache> {
        private String key;
        private Cache data;

        public static <C> CacheData<C> build(BaseDataCacheKey key, String hashKey) {
            return new CacheData<>(getOnlyKey(key, hashKey), null);
        }

        public static <C> CacheData<C> build(BaseDataCacheKey key, String hashKey, C data) {
            return new CacheData<>(getOnlyKey(key, hashKey), data);
        }

        private static String getOnlyKey(BaseDataCacheKey key, String hashKey) {
            return StrUtil.format("{}:{}", key.getPrefix(), hashKey);
        }
    }

    //==================================================================================================================

}