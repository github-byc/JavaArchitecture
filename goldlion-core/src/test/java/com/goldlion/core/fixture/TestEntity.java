package com.goldlion.core.fixture;

import com.baomidou.mybatisplus.annotation.TableField;
import com.goldlion.core.framework.crud.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 单元测试专用实体
 */
@Getter
@Setter
public class TestEntity extends BaseEntity {

    @TableField("name")
    private String name;

    @TableField("quantity")
    private Integer quantity;

    /**
     * 故意不标注@TableField,用于验证列名反解的报错路径
     */
    private String noTableField;

}
