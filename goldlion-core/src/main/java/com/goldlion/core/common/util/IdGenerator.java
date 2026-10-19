package com.goldlion.core.common.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.spring.SpringUtil;

/**
 * 统一ID生成器
 * <p>默认沿用hutool雪花算法(按MAC/PID推导workerId)。容器化部署时容器MAC可能重复,
 * 多实例下存在生成重复主键的风险,生产环境请通过配置显式指定:
 * goldlion.id.worker-id(0~31)、goldlion.id.datacenter-id(0~31),
 * K8s下可用StatefulSet序号或环境变量注入</p>
 */
public class IdGenerator {

    private static volatile Snowflake SNOWFLAKE;

    private IdGenerator() {
    }

    /**
     * 生成字符串形式的全局唯一ID
     */
    public static String nextIdStr() {
        return String.valueOf(nextId());
    }

    /**
     * 生成全局唯一ID
     */
    public static long nextId() {
        return snowflake().nextId();
    }

    private static Snowflake snowflake() {
        if (SNOWFLAKE == null) {
            synchronized (IdGenerator.class) {
                if (SNOWFLAKE == null) {
                    long workerId = getProperty("goldlion.id.worker-id");
                    long datacenterId = getProperty("goldlion.id.datacenter-id");
                    if (workerId >= 0 && datacenterId >= 0) {
                        SNOWFLAKE = new Snowflake(workerId, datacenterId);
                    } else {
                        // 未配置时回退默认推导(MAC/PID),行为与原IdUtil.getSnowflakeNextIdStr一致
                        SNOWFLAKE = IdUtil.getSnowflake();
                    }
                }
            }
        }
        return SNOWFLAKE;
    }

    private static long getProperty(String key) {
        try {
            return Convert.toLong(SpringUtil.getProperty(key), -1L);
        } catch (Exception e) {
            // Spring上下文未就绪时按未配置处理
            return -1L;
        }
    }

}
