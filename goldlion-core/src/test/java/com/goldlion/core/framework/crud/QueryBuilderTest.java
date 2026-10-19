package com.goldlion.core.framework.crud;

import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.goldlion.core.fixture.TestEntity;
import com.goldlion.core.framework.crud.dto.QueryDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * QueryBuilder 链式查询构建测试
 */
class QueryBuilderTest {

    @Test
    @DisplayName("链式条件构建:等值+LIKE+IN+范围+排序")
    void buildConditions() {
        QueryDTO dto = QueryBuilder.builder(QueryDTO.class)
                .eq(TestEntity::getName, "abc")
                .like(TestEntity::getName, "mid")
                .in(TestEntity::getQuantity, List.of(1, 2, 3))
                .ge(TestEntity::getQuantity, 10)
                .BETWEEN(TestEntity::getName, "a", "z")
                .orderByDESC(TestEntity::getQuantity)
                .build();

        List<QueryDTO.Condition> cs = dto.getQueryCondition();
        assertThat(cs).hasSize(5);

        QueryDTO.Condition eq = cs.get(0);
        assertThat(eq.getColumnName()).isEqualTo("name");
        assertThat(eq.getSqlKeyword()).isEqualTo(SqlKeyword.EQ.getSqlSegment());
        assertThat(eq.getValue()).isEqualTo("abc");

        QueryDTO.Condition in = cs.get(2);
        assertThat(in.getSqlKeyword()).isEqualTo(SqlKeyword.IN.getSqlSegment());
        assertThat(in.getValue()).isEqualTo("1,2,3");

        QueryDTO.Condition between = cs.get(4);
        assertThat(between.getValue()).isEqualTo("a");
        assertThat(between.getValue2()).isEqualTo("z");

        assertThat(dto.getSortName()).isEqualTo("quantity");
        assertThat(dto.getSortType()).isEqualTo(SqlKeyword.DESC.getSqlSegment());
    }

    @Test
    @DisplayName("QueryBuilder.builder 也可以包装已有QueryDTO")
    void wrapExisting() {
        QueryDTO existing = new QueryDTO();
        QueryDTO built = QueryBuilder.builder(existing).eq(TestEntity::getName, "x").build();
        assertThat(built).isSameAs(existing);
        assertThat(built.getQueryCondition()).hasSize(1);
    }

}
