package com.goldlion.core.framework.mastersub.service;

import com.goldlion.core.framework.crud.dto.BaseDTO;
import com.goldlion.core.framework.crud.dto.QueryDTO;
import com.goldlion.core.framework.crud.service.BaseService;
import com.goldlion.core.framework.mastersub.entity.SubBaseEntity;

import java.util.Set;

/**
 * 主子表-子表基础服务类
 */
public interface SubBaseService<T extends SubBaseEntity, BDTO extends BaseDTO, QDTO extends QueryDTO>
        extends BaseService<T, BDTO, QDTO> {

    /**
     * 根据主表id删除子表
     *
     * @param masterId 主表ID
     */
    void deleteByMasterId(String masterId);

    /**
     * 根据主表id批量删除子表
     *
     * @param masterIds 主表ID
     */
    void deleteByMasterId(Set<String> masterIds);
}
