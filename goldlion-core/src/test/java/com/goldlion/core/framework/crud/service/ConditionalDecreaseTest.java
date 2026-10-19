package com.goldlion.core.framework.crud.service;

import cn.hutool.core.util.ReflectUtil;
import com.goldlion.core.common.exception.BusinessException;
import com.goldlion.core.fixture.TestEntity;
import com.goldlion.core.framework.crud.dao.BaseDao;
import com.goldlion.core.framework.crud.dto.BaseDTO;
import com.goldlion.core.framework.crud.dto.QueryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * conditionalDecrease 条件扣减模板测试(参数校验与列名反解;SQL语义由MyBatis-Plus保证,执行路径需集成环境)
 */
class ConditionalDecreaseTest {

    private MybatisPlusServiceImpl<BaseDao<TestEntity>, TestEntity, BaseDTO, QueryDTO> impl;

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    void setUp() {
        impl = new MybatisPlusServiceImpl<BaseDao<TestEntity>, TestEntity, BaseDTO, QueryDTO>() {
        };
        ReflectUtil.setFieldValue(impl, "entityClass", TestEntity.class);
    }

    @Test
    @DisplayName("主键为空:拒绝")
    void blankIdRejected() {
        assertThatThrownBy(() -> impl.conditionalDecrease(TestEntity::getQuantity, " ", 5))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("扣减参数不完整");
    }

    @Test
    @DisplayName("扣减数量非正数:拒绝(0/负数/null)")
    void nonPositiveDeltaRejected() {
        assertThatThrownBy(() -> impl.conditionalDecrease(TestEntity::getQuantity, "1", 0))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("扣减数量必须为正数");
        assertThatThrownBy(() -> impl.conditionalDecrease(TestEntity::getQuantity, "1", -3))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("扣减数量必须为正数");
        assertThatThrownBy(() -> impl.conditionalDecrease(TestEntity::getQuantity, "1", null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("扣减数量必须为正数");
    }

    @Test
    @DisplayName("字段缺@TableField:拒绝(无法映射到列)")
    void fieldWithoutTableFieldRejected() {
        assertThatThrownBy(() -> impl.conditionalDecrease(TestEntity::getNoTableField, "1", 5))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("@TableField");
    }

    @Test
    @DisplayName("列lambda为空:拒绝")
    void nullColumnRejected() {
        assertThatCode(() -> impl.conditionalDecrease(null, "1", 5))
                .isInstanceOf(BusinessException.class);
    }

}
