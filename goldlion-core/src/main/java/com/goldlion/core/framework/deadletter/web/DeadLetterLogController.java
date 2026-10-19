package com.goldlion.core.framework.deadletter.web;

import com.goldlion.core.common.domain.Result;
import com.goldlion.core.framework.deadletter.service.DeadLetterLogService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 死信日志控制器
 */
@RestController
@RequestMapping("/deadLetter")
@RequiredArgsConstructor
public class DeadLetterLogController {
    private final DeadLetterLogService deadLetterLogService;

    //==================================================================================================================

    @Operation(summary = "查询数据缓存")
    @GetMapping(value = "/search")
    public Result search() {
        return Result.success(this.deadLetterLogService.searchDataCache());
    }

    @Operation(summary = "同步数据缓存")
    @GetMapping(value = "/synchronize")
    public Result synchronize() {
        return this.deadLetterLogService.synchronizeDataCache();
    }

    @Operation(summary = "重新投递")
    @GetMapping(value = "/reDelivery/{id}")
    private Result reDelivery(@PathVariable("id") String id) {
        return this.deadLetterLogService.reDeliveryMQ(id);
    }

}
