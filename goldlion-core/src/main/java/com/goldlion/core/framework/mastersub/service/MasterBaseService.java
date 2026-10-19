package com.goldlion.core.framework.mastersub.service;

import com.goldlion.core.framework.crud.dto.BaseDTO;
import com.goldlion.core.framework.crud.dto.QueryDTO;
import com.goldlion.core.framework.crud.service.BaseService;
import com.goldlion.core.framework.mastersub.entity.MasterBaseEntity;

/**
 * 主子表-主表基础服务类
 */
public interface MasterBaseService<T extends MasterBaseEntity, BDTO extends BaseDTO, QDTO extends QueryDTO>
        extends BaseService<T, BDTO, QDTO> {

    /**
     * 提交
     */
    void submit(String id);

    /**
     * 审核
     */
    void audit(String id);

    /**
     * 作废
     */
    void invalid(String id);

}
