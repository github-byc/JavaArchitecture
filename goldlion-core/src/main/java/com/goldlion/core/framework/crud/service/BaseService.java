package com.goldlion.core.framework.crud.service;

import cn.hutool.core.lang.func.Func1;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.goldlion.core.framework.crud.dto.BaseDTO;
import com.goldlion.core.framework.crud.dto.QueryDTO;
import com.goldlion.core.framework.crud.entity.BaseEntity;

import java.util.List;
import java.util.Set;

/**
 * 基础服务类
 *
 * @param <BDTO> 数据DTO
 * @param <QDTO> 查询DTO
 */
public interface BaseService<T extends BaseEntity, BDTO extends BaseDTO, QDTO extends QueryDTO> {

    /**
     * 创建
     *
     * @param dto 要创建的数据对象
     * @return 创建操作后的对象id
     */
    String create(BDTO dto);

    /**
     * 批量创建
     *
     * @param dtos 要批量创建的数据对象集合
     * @return 批量创建操作后有id的数据对象集合
     */
    List<T> batchCreate(List<BDTO> dtos);

    /**
     * 更新
     *
     * @param dto 要更新的数据对象
     */
    void update(BDTO dto);

    /**
     * 批量更新
     *
     * @param dtos 要批量更新的数据对象集合
     */
    void batchUpdate(List<BDTO> dtos);

    /**
     * 删除
     *
     * @param id 要删除的数据对象id
     */
    void delete(String id);

    /**
     * 删除
     *
     * @param ids 要删除的数据对象id集合
     */
    void deletes(Set<String> ids);

    /**
     * 根据主键获取数据对象
     *
     * @param id 数据对象id
     * @return 数据对象
     */
    T getById(String id);

    /**
     * 根据主键集合获取数据对象集合
     *
     * @param ids 数据对象id集合
     * @return 数据对象集合
     */
    List<T> getByIds(Set<String> ids);

    /**
     * 按条件查询数据条数
     *
     * @param dto 查询对象
     * @return 数据条数
     */
    long getCount(QDTO dto);

    /**
     * 按条件查询单个数据对象
     *
     * @param dto 查询对象
     * @return 单个数据对象
     */
    T getOne(QDTO dto);

    /**
     * 按条件查询数据对象集合
     *
     * @param dto 查询对象
     * @return 数据对象集合
     */
    List<T> search(QDTO dto);

    /**
     * 按条件分页查询数据对象集合
     *
     * @param dto      查询对象
     * @param pageNum  页码
     * @param pageSize 页面大小
     * @return 分页数据对象
     */
    Page<T> search(QDTO dto, int pageNum, int pageSize);

    /**
     * 按条件查询指定字段数据
     *
     * @param dto    查询对象
     * @param column 指定字段数据
     * @param <V>    指定字段类型
     * @return 指定字段
     */
    <V> Set<V> searchColumn(QDTO dto, Func1<T, V> column);

    <DTO> List<DTO> searchJoinList(Class<DTO> clazz, MPJLambdaWrapper<?> wrapper);

}
