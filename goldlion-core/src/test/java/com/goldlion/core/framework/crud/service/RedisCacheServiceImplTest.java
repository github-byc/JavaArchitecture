package com.goldlion.core.framework.crud.service;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.goldlion.core.fixture.TestEntity;
import com.goldlion.core.framework.crud.dao.BaseDao;
import com.goldlion.core.framework.crud.dto.BaseDTO;
import com.goldlion.core.framework.crud.dto.QueryDTO;
import com.goldlion.core.framework.crud.rediskey.BaseDataCacheKey;
import com.goldlion.core.middleware.redis.RedisOperator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.support.StaticApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Redis 读穿缓存与删除逻辑测试(按id维度缓存)
 */
class RedisCacheServiceImplTest {

    private RedisCacheServiceImpl<BaseDao<TestEntity>, TestEntity, BaseDTO, QueryDTO> impl;
    private BaseDao<TestEntity> dao;
    private RedisOperator redisOperator;

    @BeforeAll
    static void initSpringUtil() {
        // BaseKeyPrefix前缀读取依赖SpringUtil,测试环境给一个空上下文即可(setApplicationContext为实例方法,静态字段由其赋值)
        StaticApplicationContext ctx = new StaticApplicationContext();
        ctx.refresh();
        new SpringUtil().setApplicationContext(ctx);
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        dao = Mockito.mock(BaseDao.class);
        redisOperator = Mockito.mock(RedisOperator.class);
        impl = new RedisCacheServiceImpl<BaseDao<TestEntity>, TestEntity, BaseDTO, QueryDTO>() {
        };
        ReflectUtil.setFieldValue(impl, "baseMapper", dao);
        ReflectUtil.setFieldValue(impl, "entityClass", TestEntity.class);
        ReflectUtil.setFieldValue(impl, "redisOperator", redisOperator);
    }

    private TestEntity entity(String id) {
        TestEntity e = new TestEntity();
        e.setId(id);
        e.setName("n-" + id);
        return e;
    }

    @Test
    @DisplayName("缓存未命中:回源数据库并回写缓存")
    void missThenLoadFromDbAndSetCache() {
        when(redisOperator.getHashValue(any(BaseDataCacheKey.class), eq("1"))).thenReturn(null);
        when(dao.selectById("1")).thenReturn(entity("1"));

        TestEntity result = impl.getById("1");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("1");
        verify(redisOperator).putHashValue(any(BaseDataCacheKey.class), eq("1"), any(TestEntity.class));
    }

    @Test
    @DisplayName("缓存命中:不再查数据库、不重复回写")
    void hitAvoidsDb() {
        when(redisOperator.getHashValue(any(BaseDataCacheKey.class), eq("1"))).thenReturn(entity("1"));

        TestEntity result = impl.getById("1");

        assertThat(result).isNotNull();
        verify(dao, never()).selectById(anyString());
        verify(redisOperator, never()).putHashValue(any(), anyString(), any());
    }

    @Test
    @DisplayName("数据库也为空:不回写缓存(防穿透写放大)")
    void nullDataNotCached() {
        when(redisOperator.getHashValue(any(BaseDataCacheKey.class), eq("1"))).thenReturn(null);
        when(dao.selectById("1")).thenReturn(null);

        assertThat(impl.getById("1")).isNull();
        verify(redisOperator, never()).putHashValue(any(), anyString(), any());
    }

    @Test
    @DisplayName("delCache:无事务上下文时立即删除id缓存与查询缓存")
    void delCacheWithoutTransaction() {
        impl.delCache(new Object[]{"1", "2"});

        verify(redisOperator).deleteHashValue(any(BaseDataCacheKey.class), any(Object[].class));
        verify(redisOperator).deleteAllChildren(any(BaseDataCacheKey.class));
    }

}
