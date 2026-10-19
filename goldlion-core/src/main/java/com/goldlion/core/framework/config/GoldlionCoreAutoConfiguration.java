package com.goldlion.core.framework.config;

import com.goldlion.core.common.interceptor.TraceIdInterceptor;
import com.goldlion.core.common.web.global.DefaultGlobalExceptionHandler;
import com.goldlion.core.framework.deadletter.listener.DeadLetterMessageListener;
import com.goldlion.core.framework.deadletter.service.DeadLetterLogService;
import com.goldlion.core.framework.deadletter.web.DeadLetterLogController;
import com.goldlion.core.middleware.mq.producer.RabbitProducer;
import com.goldlion.core.middleware.redis.RedisOperator;
import com.goldlion.core.middleware.redis.config.RedisConfig;
import cn.hutool.extra.spring.SpringUtil;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;

/**
 * goldlion-core自动装配入口
 * <p>业务模块只要依赖core即可获得:MP分页/乐观锁插件、JsonRedisTemplate、RedisOperator、
 * MQ生产者、死信监听与死信日志端点、全局异常处理、TraceId拦截器,
 * 不再依赖业务模块的包扫描路径覆盖到com.goldlion.core</p>
 * <p>业务模块通过@ComponentScan扫描到同名Bean时与自动装配等价,不会冲突;
 * 需要整体关闭时配置goldlion.core.enabled=false</p>
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "goldlion.core", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import({
        // hutool SpringUtil(基础配置项读取依赖它:Redis前缀、ID生成器、Excel行数等)
        SpringUtil.class,
        // MyBatis-Plus分页/乐观锁插件
        MybatisPlusPluginsConfig.class,
        // JsonRedisTemplate
        RedisConfig.class,
        // Web拦截器(TraceId生成、SecurityContextHolder清理)
        CoreWebMvcConfig.class,
        TraceIdInterceptor.class,
        // 全局异常处理
        DefaultGlobalExceptionHandler.class,
        // Redis操作工具
        RedisOperator.class,
        // MQ生产者与死信处理
        RabbitProducer.class,
        DeadLetterLogService.class,
        DeadLetterMessageListener.class,
        DeadLetterLogController.class
})
public class GoldlionCoreAutoConfiguration {

}
