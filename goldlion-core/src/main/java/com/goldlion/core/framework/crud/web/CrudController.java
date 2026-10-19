package com.goldlion.core.framework.crud.web;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.goldlion.core.common.domain.dto.BaseIdDTO;
import com.goldlion.core.common.domain.dto.BaseIdsDTO;
import com.goldlion.core.common.exception.BusinessException;
import com.goldlion.core.common.validation.CreateGroup;
import com.goldlion.core.common.validation.UpdateGroup;
import com.goldlion.core.common.web.BaseController;
import com.goldlion.core.framework.crud.dto.BaseDTO;
import com.goldlion.core.framework.crud.dto.QueryDTO;
import com.goldlion.core.framework.crud.entity.BaseEntity;
import com.goldlion.core.framework.crud.service.BaseService;
import com.goldlion.core.framework.crud.vo.BaseVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 增删改查控制器
 */
public abstract class CrudController<
        BS extends BaseService<? extends BaseEntity, BDTO, QDTO>,
        BDTO extends BaseDTO,
        QDTO extends QueryDTO,
        VO extends BaseVO> extends BaseController {

    protected BS baseService;
    protected Class<BDTO> dtoClass;
    protected Class<VO> voClass;

    public CrudController() {
        Type[] types = TypeUtil.getTypeArguments(this.getClass());
        this.baseService = SpringUtil.getBean((Class<BS>) types[0]);
        this.dtoClass = (Class<BDTO>) types[1];
        this.voClass = (Class<VO>) types[3];
    }

    @Operation(summary = "创建")
    @PostMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public String create(@RequestBody @Validated(value = CreateGroup.class) BDTO dto) {
        return this.baseService.create(dto);
    }

    @Operation(summary = "更新")
    @PutMapping
    public void update(@RequestBody @Validated(value = UpdateGroup.class) BDTO dto) {
        this.baseService.update(dto);
    }

    @Operation(summary = "删除")
    @DeleteMapping
    public void delete(@RequestBody @Validated BaseIdsDTO dto) {
        this.baseService.deletes(dto.getIds());
    }

    @Operation(summary = "主键查询")
    @PostMapping(value = "/id")
    public VO getById(@RequestBody @Validated BaseIdDTO dto) {
        BaseEntity entity = this.baseService.getById(dto.getId());
        if (Objects.isNull(entity)) {
            throw new BusinessException("数据不存在,请检查!");
        }
        VO vo = BeanUtil.toBean(entity, this.voClass);
        this.fillVo(vo);
        return vo;
    }

    @Operation(summary = "主键集合查询")
    @PostMapping(value = "/ids")
    public List<VO> getByIds(@RequestBody @Validated BaseIdsDTO dto) {
        List<? extends BaseEntity> entities = this.baseService.getByIds(dto.getIds());
        List<VO> vos = BeanUtil.copyToList(entities, this.voClass);
        this.fillVo(vos);
        return vos;
    }

    @Operation(summary = "条件查询单个")
    @PostMapping(value = "/one")
    public VO getOne(@RequestBody QDTO dto) {
        BaseEntity entity = this.baseService.getOne(dto);
        if (Objects.isNull(entity)) {
            throw new BusinessException("未查询到符合条件的数据,请检查查询条件!");
        }
        VO vo = BeanUtil.toBean(entity, this.voClass);
        this.fillVo(vo);
        return vo;
    }

    @Operation(summary = "条件查询集合")
    @PostMapping(value = "/search")
    public List<VO> search(@RequestBody @Validated QDTO dto) {
        this.beforeSearch(dto);

        List<? extends BaseEntity> entities = this.baseService.search(dto);
        List<VO> vos = BeanUtil.copyToList(entities, this.voClass);
        this.fillVo(vos);
        return vos;
    }

    @Operation(summary = "条件查询分页集合")
    @PostMapping(value = "/search/{pageNum}/{pageSize}")
    public Page<VO> search(@RequestBody @Validated QDTO dto, @PathVariable("pageNum") int pageNum, @PathVariable("pageSize") int pageSize) {
        this.beforeSearch(dto);

        Page<? extends BaseEntity> entityPage = this.baseService.search(dto, pageNum, pageSize);
        List<VO> bdList = BeanUtil.copyToList(entityPage.getRecords(), this.voClass);
        Page<VO> bdPage = PageDTO.of(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal(), entityPage.searchCount());
        bdPage.setRecords(bdList);
        this.fillVo(bdPage.getRecords());
        return bdPage;
    }

    //==================================================================================================================

    protected void fillVo(VO vo) {
        this.fillVo(Collections.singletonList(vo));
    }

    protected void fillVo(List<VO> vos) {
    }

    /**
     * 查询之前执行逻辑
     */
    protected void beforeSearch(QDTO dto) {
    }

}
