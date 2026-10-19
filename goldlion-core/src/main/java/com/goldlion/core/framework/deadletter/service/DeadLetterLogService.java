package com.goldlion.core.framework.deadletter.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.goldlion.core.common.domain.Result;
import com.goldlion.core.framework.deadletter.model.DeadLetterLog;
import com.goldlion.core.framework.deadletter.rediskey.DeadLetterDataCacheKey;
import com.goldlion.core.middleware.mq.producer.RabbitProducer;
import com.goldlion.core.middleware.redis.RedisOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 死信日志服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeadLetterLogService {
    private static final String INSERT_SQL = "INSERT INTO `dead_letter_log` (`id`,`created_on`,`reason`,`count`,`time`,`exchange`,`scene`,`queue`,`routing_key`,`message`) VALUES (:id,:createdOn,:reason,:count,:time,:exchange,:scene,:queue,:routingKey,:message)";
    private static final String DELETE_SQL = "DELETE FROM `dead_letter_log` WHERE id = :id";
    private static final String SELECT_SQL = "SELECT * FROM `dead_letter_log` ORDER BY id ASC";
    private static final String GET_BY_ID_SQL = "SELECT * FROM `dead_letter_log` WHERE id = :id";

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final RedisOperator redisOperator;
    private final RedissonClient redissonClient;
    private final RabbitProducer rabbitProducer;

    //==================================================================================================================

    public void insert(DeadLetterLog deadLetterLog) {
        SqlParameterSource sps = new BeanPropertySqlParameterSource(deadLetterLog);
        this.namedParameterJdbcTemplate.update(INSERT_SQL, sps);
        this.redisOperator.putHashValue(DeadLetterDataCacheKey.LIST, deadLetterLog.getId(), deadLetterLog);
    }

    private void delete(String id) {
        SqlParameterSource sps = new MapSqlParameterSource("id", id);
        this.namedParameterJdbcTemplate.update(DELETE_SQL, sps);
        this.redisOperator.deleteHashValue(DeadLetterDataCacheKey.LIST, new Object[]{id});
    }

    public DeadLetterLog getById(String id) {
        SqlParameterSource sps = new MapSqlParameterSource("id", id);
        RowMapper<DeadLetterLog> rowMapper = BeanPropertyRowMapper.newInstance(DeadLetterLog.class);
        return this.namedParameterJdbcTemplate.queryForObject(GET_BY_ID_SQL, sps, rowMapper);
    }

    //==================================================================================================================

    /**
     * 查询数据缓存
     */
    public List<DeadLetterLog> searchDataCache() {
        List<Object> objects = this.redisOperator.valuesHashValue(DeadLetterDataCacheKey.LIST);
        return BeanUtil.copyToList(objects, DeadLetterLog.class);
    }

    /**
     * 同步数据缓存
     */
    public Result synchronizeDataCache() {
        this.redisOperator.deleteAll(DeadLetterDataCacheKey.LIST);

        RowMapper<DeadLetterLog> rowMapper = BeanPropertyRowMapper.newInstance(DeadLetterLog.class);
        List<DeadLetterLog> deadLetterLogList = this.namedParameterJdbcTemplate.query(SELECT_SQL, rowMapper);
        if (CollUtil.isNotEmpty(deadLetterLogList)) {
            Map<String, DeadLetterLog> map = deadLetterLogList.stream().collect(Collectors.toMap(DeadLetterLog::getId, Function.identity()));
            this.redisOperator.putAllHashValue(DeadLetterDataCacheKey.LIST, map);
        }

        return Result.success(deadLetterLogList.size());
    }

    /**
     * 重新投递MQ
     */
    public Result reDeliveryMQ(String id) {
        RLock lock = this.redissonClient.getLock(id);

        // 添加锁防止重复投递
        if (lock.tryLock()) {
            try {
                Object object = this.redisOperator.getHashValue(DeadLetterDataCacheKey.LIST, id);
                if (Objects.isNull(object)) {
                    return Result.error("未找到该任务,该任务或已执行!");
                }

                DeadLetterLog deadLetterLog = BeanUtil.toBean(object, DeadLetterLog.class);
                this.rabbitProducer.reDelivery(deadLetterLog);
                this.delete(id);

            } finally {
                // 释放锁
                lock.unlock();
            }
            return Result.success();
        } else {
            return Result.error("该任务正在执行中....");
        }

    }

}
