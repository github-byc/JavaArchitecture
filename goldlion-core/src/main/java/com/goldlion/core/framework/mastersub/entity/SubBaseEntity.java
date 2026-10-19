package com.goldlion.core.framework.mastersub.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.goldlion.core.framework.crud.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 主子表-子表实体
 */
@Getter
@Setter
public abstract class SubBaseEntity extends BaseEntity {

    @TableField("master_id")
    @Schema(description = "主表ID")
    private String masterId;

}
