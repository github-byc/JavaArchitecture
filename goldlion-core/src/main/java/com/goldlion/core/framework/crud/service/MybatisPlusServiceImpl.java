package com.goldlion.core.framework.crud.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.*;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.goldlion.core.common.context.SecurityContextHolder;
import com.goldlion.core.common.exception.BusinessException;
import com.goldlion.core.common.util.IdGenerator;
import com.goldlion.core.framework.crud.dao.BaseDao;
import com.goldlion.core.framework.crud.dto.BaseDTO;
import com.goldlion.core.framework.crud.dto.QueryDTO;
import com.goldlion.core.framework.crud.entity.BaseEntity;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * MybatisPlus 基础服务实现类
 *
 * @param <M>    Mapper
 * @param <T>    持久化对象
 * @param <BDTO> 数据DTO
 * @param <QDTO> 查询DTO
 */
public abstract class MybatisPlusServiceImpl<M extends BaseDao<T>, T extends BaseEntity, BDTO extends BaseDTO, QDTO extends QueryDTO>
        extends MPJBaseServiceImpl<M, T>
        implements BaseService<T, BDTO, QDTO> {

    protected Class<BDTO> baseDTOClass;
    protected Class<QDTO> queryDTOClass;

    public MybatisPlusServiceImpl() {
        Type[] types = TypeUtil.getTypeArguments(this.getClass());
        this.baseDTOClass = (Class<BDTO>) types[2];
        this.queryDTOClass = (Class<QDTO>) types[3];
    }

    //==================================================================================================================

    @Transactional
    @Override
    public String create(BDTO dto) {
        T entity = BeanUtil.toBean(dto, super.getEntityClass());
        initCreateInfo(entity);

        this.beforeCreate(entity);
        super.save(entity);
        this.afterCreate(entity);

        return entity.getId();
    }

    /**
     * 创建之前需要处理的逻辑
     */
    public void beforeCreate(T entity) {
    }

    /**
     * 创建之后需要处理的逻辑
     */
    public void afterCreate(T entity) {
    }

    @Transactional
    @Override
    public List<T> batchCreate(List<BDTO> dtos) {
        List<T> es = dtos.stream().map(bd -> {
            T entity = BeanUtil.toBean(bd, super.getEntityClass());
            initCreateInfo(entity);
            return entity;
        }).toList();

        this.beforeCreate(es);
        super.saveBatch(es);
        this.afterCreate(es);

        return es;
    }

    /**
     * 批量创建之前需要处理的逻辑
     */
    public void beforeCreate(List<T> es) {
    }

    /**
     * 批量创建之后需要处理的逻辑
     */
    public void afterCreate(List<T> es) {
    }

    @Transactional
    @Override
    public void update(BDTO dto) {
        if (StrUtil.isBlank(dto.getId())) {
            throw new BusinessException("ID为空,请检查");
        }

        T entity = this.getById(dto.getId());
        // 部分更新语义:忽略DTO中的null字段(未传字段保持库内原值),与MP默认的SQL层null忽略策略对齐,
        // 钩子拿到的实体状态与最终落库状态一致;如需清空字段请提供专用端点显式处理
        BeanUtil.copyProperties(dto, entity, CopyOptions.create().setIgnoreNullValue(true).setIgnoreProperties("id"));
        initUpdateInfo(entity);

        this.beforeUpdate(entity);
        super.updateById(entity);
        this.afterUpdate(entity);
    }

    /**
     * 更新之前需要处理的逻辑
     */
    public void beforeUpdate(T entity) {
    }

    /**
     * 更新之后需要处理的逻辑
     */
    public void afterUpdate(T entity) {
    }

    @Transactional
    @Override
    public void batchUpdate(List<BDTO> dtos) {
        if (dtos.stream().anyMatch(bd -> StrUtil.isBlank(bd.getId()))) {
            throw new BusinessException("ID为空,请检查");
        }

        Map<String, BDTO> dtoMap = dtos.stream().collect(Collectors.toMap(BaseDTO::getId, Function.identity()));
        List<T> es = this.getByIds(dtoMap.keySet());

        es.forEach(e -> {
            if (dtoMap.containsKey(e.getId())) {
                BDTO dto = dtoMap.get(e.getId());
                // 部分更新语义,同update:忽略null字段
                BeanUtil.copyProperties(dto, e, CopyOptions.create().setIgnoreNullValue(true).setIgnoreProperties("id"));
                initUpdateInfo(e);
            }
        });

        this.beforeUpdate(es);
        super.updateBatchById(es);
        this.afterUpdate(es);
    }

    /**
     * 批量更新之前需要处理的逻辑
     */
    public void beforeUpdate(List<T> es) {
    }

    /**
     * 批量更新之后需要处理的逻辑
     */
    public void afterUpdate(List<T> es) {
    }

    @Transactional
    @Override
    public void delete(String id) {
        if (StrUtil.isBlank(id)) {
            throw new BusinessException("ID为空,请检查");
        }

        this.beforeDelete(id);
        ChainWrappers.lambdaUpdateChain(super.getEntityClass()).eq(BaseEntity::getId, id).remove();
        this.afterDelete(id);
    }

    /**
     * 删除之前需要处理的逻辑
     */
    public void beforeDelete(String id) {
    }

    /**
     * 删除之后需要处理的逻辑
     */
    public void afterDelete(String id) {
    }

    @Transactional
    @Override
    public void deletes(Set<String> ids) {
        if (ids.stream().anyMatch(StrUtil::isBlank)) {
            throw new BusinessException("ID为空,请检查");
        }

        this.beforeDelete(ids);
        ChainWrappers.lambdaUpdateChain(super.getEntityClass()).in(BaseEntity::getId, ids).remove();
        this.afterDelete(ids);
    }

    /**
     * 批量删除之前需要处理的逻辑
     */
    public void beforeDelete(Set<String> ids) {
    }

    /**
     * 批量删除之后需要处理的逻辑
     */
    public void afterDelete(Set<String> ids) {
    }

    //==================================================================================================================

    @Override
    public T getById(String id) {
        if (StrUtil.isBlank(id)) {
            return null;
        }

        return super.getById(id);
    }

    @Override
    public List<T> getByIds(Set<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }

        return super.listByIds(ids);
    }

    @Override
    public long getCount(QDTO dto) {
        return super.count(assemblyConditionWhere(dto));
    }

    @Override
    public T getOne(QDTO dto) {
        try {
            return super.getOne(assemblyConditionWhere(dto));
        } catch (TooManyResultsException e) {
            // 多行结果对用户表现为500,转成业务异常给出可理解的提示
            throw new BusinessException("条件查询到多条数据,请精确查询条件!");
        }
    }

    @Override
    public List<T> search(QDTO dto) {
        Page<T> page = this.searchPage(dto);
        return page.getRecords();
    }

    @Override
    public Page<T> search(QDTO dto, int pageNum, int pageSize) {
        return this.searchPage(dto, pageNum, pageSize);
    }

    @Override
    public <V> Set<V> searchColumn(QDTO dto, Func1<T, V> column) {
        String columnName = LambdaUtil.getFieldName(column);
        Page<T> page = this.searchPage(dto, this.getColumnName(columnName));
        return page.getRecords().stream().filter(Objects::nonNull).map(column::callWithRuntimeException).collect(Collectors.toSet());
    }

    @Override
    public <DTO> List<DTO> searchJoinList(Class<DTO> clazz, MPJLambdaWrapper<?> wrapper) {
        return this.selectJoinList(clazz, (MPJLambdaWrapper<T>) wrapper);
    }

    /**
     * 库存/数量条件扣减模板(单行原子更新,防超卖)
     * <p>等价 SQL:UPDATE 表 SET 数量列 = 数量列 - #{delta} WHERE id = #{id} AND 数量列 &gt;= #{delta},
     * 由数据库行锁保证原子性,返回 false 即余量不足。库存/额度类扣减一律使用本方法,
     * 禁止"先查询再扣减"的两段式写法(并发下必超卖)</p>
     *
     * @param column 数量列对应的实体字段(lambda,字段需标注@TableField)
     * @param id     行主键
     * @param delta  扣减数量(必须为正数,服务端数值无注入面)
     * @return true=扣减成功;false=数据不存在或余量不足
     */
    protected boolean conditionalDecrease(Func1<T, ?> column, String id, Number delta) {
        if (Objects.isNull(column) || StrUtil.isBlank(id)) {
            throw new BusinessException("扣减参数不完整,请检查");
        }
        if (Objects.isNull(delta) || delta.doubleValue() <= 0) {
            throw new BusinessException("扣减数量必须为正数");
        }

        // 列名经实体字段反解(服务端可信),delta为服务端数值,均无注入面
        String columnName = this.getColumnName(LambdaUtil.getFieldName(column));
        UpdateWrapper<T> wrapper = Wrappers.<T>update()
                .setSql(StrUtil.format("{} = {} - {}", columnName, columnName, delta))
                .eq("id", id)
                .ge(columnName, delta);
        return super.update(wrapper);
    }

    //==================================================================================================================

    private Page<T> searchPage(QDTO qd) {
        return this.searchPage(qd, new String[0]);
    }

    private Page<T> searchPage(QDTO qd, String... columnNames) {
        return this.searchPage(qd, 0, Integer.MAX_VALUE, columnNames);
    }

    /**
     * 查询
     *
     * @param qd          查询对象
     * @param pageNum     页码
     * @param pageSize    页面大小
     * @param columnNames 指定字段名称
     * @return 分页数据对象
     */
    private Page<T> searchPage(QDTO qd, int pageNum, int pageSize, String... columnNames) {
        QueryWrapper<T> queryWrapper = assemblyConditionWhere(qd);
        if (ArrayUtil.isNotEmpty(columnNames)) {
            queryWrapper.select(columnNames);
        }

        return super.page(Page.of(pageNum, pageSize), queryWrapper);
    }

    //==================================================================================================================

    /**
     * 动态组装查询条件
     *
     * @param qd 查询对象
     * @return 组装好地查询条件
     */
    private QueryWrapper<T> assemblyConditionWhere(QDTO qd) {
        QueryWrapper<T> queryWrapper = Wrappers.query();

        if (Objects.nonNull(qd) && CollUtil.isNotEmpty(qd.getQueryCondition())) {
            qd.getQueryCondition().stream()
                    // 判断查询的字段值是否为空，为空不加入查询
                    .filter(condition -> isType(condition.getValue()))
                    .forEach(condition -> {
                        String columnName = condition.getColumnName();
                        String sqlKeyword = condition.getSqlKeyword();
                        Object value = condition.getValue();

                        if (!"id".equalsIgnoreCase(columnName)) {
                            columnName = getColumnName(columnName);
                        }

                        // 根据匹配的方式，进行组装
                        if (SqlKeyword.EQ.getSqlSegment().equalsIgnoreCase(sqlKeyword) || SqlKeyword.EQ.name().equalsIgnoreCase(sqlKeyword)) {
                            queryWrapper.eq(columnName, value);
                        } else if (SqlKeyword.NE.getSqlSegment().equalsIgnoreCase(sqlKeyword) || SqlKeyword.NE.name().equalsIgnoreCase(sqlKeyword)) {
                            queryWrapper.ne(columnName, value);
                        } else if (SqlKeyword.GT.getSqlSegment().equalsIgnoreCase(sqlKeyword) || SqlKeyword.GT.name().equalsIgnoreCase(sqlKeyword)) {
                            queryWrapper.gt(columnName, value);
                        } else if (SqlKeyword.GE.getSqlSegment().equalsIgnoreCase(sqlKeyword) || SqlKeyword.GE.name().equalsIgnoreCase(sqlKeyword)) {
                            queryWrapper.ge(columnName, value);
                        } else if (SqlKeyword.LT.getSqlSegment().equalsIgnoreCase(sqlKeyword) || SqlKeyword.LT.name().equalsIgnoreCase(sqlKeyword)) {
                            queryWrapper.lt(columnName, value);
                        } else if (SqlKeyword.LE.getSqlSegment().equalsIgnoreCase(sqlKeyword) || SqlKeyword.LE.name().equalsIgnoreCase(sqlKeyword)) {
                            queryWrapper.le(columnName, value);
                        } else if (SqlKeyword.LIKE.getSqlSegment().equalsIgnoreCase(sqlKeyword)) {
                            queryWrapper.like(columnName, value);
                        } else if (SqlKeyword.NOT_LIKE.getSqlSegment().equalsIgnoreCase(sqlKeyword)) {
                            queryWrapper.notLike(columnName, value);
                        } else if (SqlKeyword.IN.getSqlSegment().equalsIgnoreCase(sqlKeyword)) {
                            if (value.toString().indexOf(",") > 0) {
                                queryWrapper.in(columnName, Arrays.asList(value.toString().split(",")));
                            } else {
                                queryWrapper.eq(columnName, value);
                            }
                        } else if (SqlKeyword.NOT_IN.getSqlSegment().equalsIgnoreCase(sqlKeyword)) {
                            if (value.toString().indexOf(",") > 0) {
                                queryWrapper.notIn(columnName, Arrays.asList(value.toString().split(",")));
                            } else {
                                queryWrapper.ne(columnName, value);
                            }
                        } else if (SqlKeyword.BETWEEN.getSqlSegment().equalsIgnoreCase(sqlKeyword)) {
                            queryWrapper.between(columnName, value, condition.getValue2());
                        } else if (SqlKeyword.IS_NULL.getSqlSegment().equalsIgnoreCase(sqlKeyword)) {
                            queryWrapper.isNull(columnName);
                        } else if (SqlKeyword.IS_NOT_NULL.getSqlSegment().equalsIgnoreCase(sqlKeyword)) {
                            queryWrapper.isNotNull(columnName);
                        } else {
                            throw new BusinessException(StrUtil.format("暂不支持匹配方式:{}", sqlKeyword));
                        }
                    });
        }

        if (StrUtil.isNotBlank(qd.getSortName())) {
            if (Objects.equals(SqlKeyword.DESC.getSqlSegment(), qd.getSortType())) {
                queryWrapper.orderByDesc(getColumnName(qd.getSortName()));
            } else {
                queryWrapper.orderByAsc(getColumnName(qd.getSortName()));
            }
        }

        return queryWrapper;
    }

    private static boolean isType(Object value) {
        boolean isType;
        if (Objects.isNull(value)) {
            isType = false;
        } else if (value instanceof String v) {
            isType = StrUtil.isNotBlank(v);
        } else if (value instanceof Iterable<?> v) {
            isType = CollUtil.isNotEmpty(v);
        } else if (value.getClass().isArray()) {
            isType = ArrayUtil.isNotEmpty(value);
        } else {
            isType = true;
        }
        return isType;
    }

    private String getColumnName(String columnName) {
        Field field = ReflectUtil.getField(this.getEntityClass(), columnName);
        if (Objects.isNull(field)) {
            throw new BusinessException(StrUtil.format("该对象没有{}字段,请检查!", columnName));
        }
        TableField tableField = field.getAnnotation(TableField.class);
        if (Objects.isNull(tableField) || StrUtil.isBlank(tableField.value())) {
            throw new BusinessException(StrUtil.format("该对象{}字段没有配置@TableField,请检查!", columnName));
        }

        return tableField.value();
    }

    /**
     * 初始化创建信息
     */
    private static <T extends BaseEntity> void initCreateInfo(T entity) {
        entity.setId(IdGenerator.nextIdStr());
        entity.setCreatedOn(LocalDateTime.now());
        entity.setCreatedBy(SecurityContextHolder.getCurrentUsername());
    }

    /**
     * 初始更新信息
     */
    private static <T extends BaseEntity> void initUpdateInfo(T entity) {
        entity.setModifiedOn(LocalDateTime.now());
        entity.setModifiedBy(SecurityContextHolder.getCurrentUsername());
    }

}
