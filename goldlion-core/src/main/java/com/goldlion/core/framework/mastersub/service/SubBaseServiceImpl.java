package com.goldlion.core.framework.mastersub.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.goldlion.core.common.exception.BusinessException;
import com.goldlion.core.framework.crud.dao.BaseDao;
import com.goldlion.core.framework.crud.dto.BaseDTO;
import com.goldlion.core.framework.crud.dto.QueryDTO;
import com.goldlion.core.framework.crud.service.MybatisPlusServiceImpl;
import com.goldlion.core.framework.mastersub.entity.SubBaseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * 主子表-子表基础服务类
 */
public class SubBaseServiceImpl<M extends BaseDao<T>, T extends SubBaseEntity, BDTO extends BaseDTO, QDTO extends QueryDTO>
        extends MybatisPlusServiceImpl<M, T, BDTO, QDTO>
        implements SubBaseService<T, BDTO, QDTO> {

    @Transactional
    @Override
    public void deleteByMasterId(String masterId) {
        if (StrUtil.isBlank(masterId)) {
            throw new BusinessException("主表ID为空,请检查");
        }

        ChainWrappers.lambdaUpdateChain(super.getEntityClass()).eq(SubBaseEntity::getMasterId, masterId).remove();
    }

    @Transactional
    @Override
    public void deleteByMasterId(Set<String> masterIds) {
        if (CollUtil.isEmpty(masterIds)) {
            throw new BusinessException("主表ID为空,请检查");
        }

        ChainWrappers.lambdaUpdateChain(super.getEntityClass()).in(SubBaseEntity::getMasterId, masterIds).remove();
    }

}
