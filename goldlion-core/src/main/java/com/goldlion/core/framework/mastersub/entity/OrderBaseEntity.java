package com.goldlion.core.framework.mastersub.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.goldlion.core.framework.crud.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 单据主子表-主表实体
 */
@Getter
@Setter
public class OrderBaseEntity extends BaseEntity {

    @TableField("order_num")
    @Schema(description = "单据编号")
    private String orderNum;

    @TableField("order_status")
    @Schema(description = "单据状态")
    private String orderStatus;

}
