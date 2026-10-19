package com.goldlion.core.framework.crud;

import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.goldlion.core.framework.crud.dto.QueryDTO;
import lombok.AllArgsConstructor;

import java.util.Collection;

/**
 * 查询构建器
 */
@AllArgsConstructor
public class QueryBuilder<QD extends QueryDTO> {
    private QD queryDTO;

    public static <T extends QueryDTO> QueryBuilder<T> builder(Class<T> clazz) {
        return new QueryBuilder<>(ReflectUtil.newInstance(clazz));
    }

    public static <T extends QueryDTO> QueryBuilder<T> builder(T queryDTO) {
        return new QueryBuilder<>(queryDTO);
    }

    //==================================================================================================================

    public <T> QueryBuilder<QD> eq(Func1<T, ?> column, Object value) {
        this.addCondition(column, SqlKeyword.EQ.getSqlSegment(), value);
        return this;
    }

    public <T> QueryBuilder<QD> ne(Func1<T, ?> column, Object value) {
        this.addCondition(column, SqlKeyword.NE.getSqlSegment(), value);
        return this;
    }

    public <T> QueryBuilder<QD> gt(Func1<T, ?> column, Object value) {
        this.addCondition(column, SqlKeyword.GT.getSqlSegment(), value);
        return this;
    }

    public <T> QueryBuilder<QD> ge(Func1<T, ?> column, Object value) {
        this.addCondition(column, SqlKeyword.GE.getSqlSegment(), value);
        return this;
    }

    public <T> QueryBuilder<QD> lt(Func1<T, ?> column, Object value) {
        this.addCondition(column, SqlKeyword.LT.getSqlSegment(), value);
        return this;
    }

    public <T> QueryBuilder<QD> le(Func1<T, ?> column, Object value) {
        this.addCondition(column, SqlKeyword.LE.getSqlSegment(), value);
        return this;
    }

    public <T> QueryBuilder<QD> BETWEEN(Func1<T, ?> column, Object value, Object value2) {
        this.addCondition(column, SqlKeyword.BETWEEN.getSqlSegment(), value, value2);
        return this;
    }

    public <T> QueryBuilder<QD> in(Func1<T, ?> column, Collection<?> values) {
        String value = StrUtil.join(",", values);
        this.addCondition(column, SqlKeyword.IN.getSqlSegment(), value);
        return this;
    }

    public <T> QueryBuilder<QD> like(Func1<T, ?> column, Object value) {
        this.addCondition(column, SqlKeyword.LIKE.getSqlSegment(), value);
        return this;
    }

    public <T> QueryBuilder<QD> orderByASC(Func1<T, ?> column) {
        this.orderBy(column, SqlKeyword.ASC.getSqlSegment());
        return this;
    }

    public <T> QueryBuilder<QD> orderByDESC(Func1<T, ?> column) {
        this.orderBy(column, SqlKeyword.DESC.getSqlSegment());
        return this;
    }

    public QD build() {
        return this.queryDTO;
    }

    private <T> void addCondition(Func1<T, ?> column, String sqlKeyword, Object value) {
        this.addCondition(column, sqlKeyword, value, null);
    }

    private <T> void addCondition(Func1<T, ?> column, String sqlKeyword, Object value, Object value2) {
        String columnName = LambdaUtil.getFieldName(column);
        this.queryDTO.getQueryCondition().add(new QueryDTO.Condition(columnName, sqlKeyword, value, value2));
    }

    private <T> void orderBy(Func1<T, ?> column, String sortType) {
        String columnName = LambdaUtil.getFieldName(column);
        this.queryDTO.setSortName(columnName);
        this.queryDTO.setSortType(sortType);
    }

}
