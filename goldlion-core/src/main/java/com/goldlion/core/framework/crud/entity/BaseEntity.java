package com.goldlion.core.framework.crud.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.Version;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 基础实体
 */
@Getter
@Setter
public abstract class BaseEntity implements java.io.Serializable {

    @TableId("id")
    @Schema(description = "主键ID")
    private String id;

    @TableField("created_by")
    @Schema(description = "创建者")
    private String createdBy;

    @TableField("created_on")
    @Schema(description = "创建日期")
    private LocalDateTime createdOn;

    @TableField("modified_by")
    @Schema(description = "修改者")
    private String modifiedBy;

    @TableField("modified_on")
    @Schema(description = "修改日期")
    private LocalDateTime modifiedOn;

    @Version
    @TableField("version")
    @Schema(description = "乐观锁")
    private Integer version = 0;

}
