package com.goldlion.core.framework.crud.service;

import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.goldlion.core.common.exception.BusinessException;
import com.goldlion.core.fixture.TestEntity;
import com.goldlion.core.framework.crud.dao.BaseDao;
import com.goldlion.core.framework.crud.dto.BaseDTO;
import com.goldlion.core.framework.crud.dto.QueryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 动态查询条件组装(assemblyConditionWhere)测试——直接验证前端筛选的拼接与安全边界
 */
class ConditionAssemblyTest {

    private MybatisPlusServiceImpl<BaseDao<TestEntity>, TestEntity, BaseDTO, QueryDTO> impl;

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    void setUp() {
        impl = new MybatisPlusServiceImpl<BaseDao<TestEntity>, TestEntity, BaseDTO, QueryDTO>() {
        };
        ReflectUtil.setFieldValue(impl, "entityClass", TestEntity.class);
    }

    @SuppressWarnings("unchecked")
    private QueryWrapper<TestEntity> assemble(QueryDTO dto) {
        try {
            java.lang.reflect.Method m = MybatisPlusServiceImpl.class.getDeclaredMethod("assemblyConditionWhere", QueryDTO.class);
            m.setAccessible(true);
            return (QueryWrapper<TestEntity>) m.invoke(impl, dto);
        } catch (java.lang.reflect.InvocationTargetException e) {
            // 反射包装的业务异常解包后再抛,保持原语义
            if (e.getCause() instanceof RuntimeException re) {
                throw re;
            }
            throw new IllegalStateException(e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private QueryDTO.Condition cond(String col, String kw, Object val, Object val2) {
        return new QueryDTO.Condition(col, kw, val, val2);
    }

    @Test
    @DisplayName("等值/LIKE/IN/BETWEEN 组装:列名与参数均正确")
    void mixedConditions() {
        QueryDTO dto = new QueryDTO();
        dto.setQueryCondition(List.of(
                cond("name", "=", "abc", null),
                cond("name", "LIKE", "mid", null),
                cond("name", "IN", "a,b", null),
                cond("quantity", "BETWEEN", 10, 20)));

        QueryWrapper<TestEntity> w = assemble(dto);
        String sql = w.getSqlSegment();
        assertThat(sql).contains("name =").contains("name LIKE").contains("name IN").contains("quantity BETWEEN");
        assertThat(w.getParamNameValuePairs().values())
                .contains("abc", "%mid%", "a", "b", 10, 20);
    }

    @Test
    @DisplayName("排序字段与方向")
    void orderBy() {
        QueryDTO dto = new QueryDTO();
        dto.setSortName("name");
        dto.setSortType("DESC");
        assertThat(assemble(dto).getSqlSegment()).containsIgnoringCase("ORDER BY name DESC");
    }

    @Test
    @DisplayName("空值条件被过滤,不进入SQL")
    void blankValueSkipped() {
        QueryDTO dto = new QueryDTO();
        dto.setQueryCondition(List.of(
                cond("name", "=", " ", null),
                cond("quantity", "=", null, null)));

        QueryWrapper<TestEntity> w = assemble(dto);
        assertThat(w.getParamNameValuePairs()).isEmpty();
    }

    @Test
    @DisplayName("不存在的实体字段:拒绝并提示(防注入面)")
    void unknownColumnRejected() {
        QueryDTO dto = new QueryDTO();
        dto.setQueryCondition(List.of(cond("nope", "=", "x", null)));
        assertThatThrownBy(() -> assemble(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("nope");
    }

    @Test
    @DisplayName("不支持的匹配方式:拒绝(白名单校验,防注入面)")
    void unsupportedKeywordRejected() {
        QueryDTO dto = new QueryDTO();
        dto.setQueryCondition(List.of(cond("name", "DROP TABLE", "x", null)));
        assertThatThrownBy(() -> assemble(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("暂不支持");
    }

    @Test
    @DisplayName("字段缺@TableField:拒绝(无法映射到列)")
    void fieldWithoutTableFieldRejected() {
        QueryDTO dto = new QueryDTO();
        dto.setQueryCondition(List.of(cond("noTableField", "=", "x", null)));
        assertThatThrownBy(() -> assemble(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("@TableField");
    }

}
