package com.goldlion.core.framework.mastersub.service;

import com.goldlion.core.common.context.SecurityContextHolder;
import com.goldlion.core.framework.crud.dao.BaseDao;
import com.goldlion.core.framework.crud.dto.QueryDTO;
import com.goldlion.core.framework.crud.service.MybatisPlusServiceImpl;
import com.goldlion.core.framework.mastersub.dto.MasterBaseDTO;
import com.goldlion.core.framework.mastersub.entity.MasterBaseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 主子表-主表基础服务类
 */
public abstract class MasterBaseServiceImpl<M extends BaseDao<T>, T extends MasterBaseEntity, BDTO extends MasterBaseDTO, QDTO extends QueryDTO>
        extends MybatisPlusServiceImpl<M, T, BDTO, QDTO>
        implements MasterBaseService<T, BDTO, QDTO> {

    @Transactional
    @Override
    public String create(BDTO dto) {
        dto.setOrderNum(this.generateOrderNum());
        dto.setOrderStatus(this.getInitialStatus());

        return super.create(dto);
    }

    @Transactional
    @Override
    public void submit(String id) {
        T entity = this.getById(id);
        entity.setOrderStatus(this.getSubmitStatus());
        this.fillUpdateInfo(entity);

        this.beforeSubmit(entity);
        super.updateById(entity);
        this.afterSubmit(entity);
    }

    /**
     * 提交之前需要处理的逻辑
     */
    public void beforeSubmit(T entity) {
    }

    /**
     * 提交之后需要处理的逻辑
     */
    public void afterSubmit(T entity) {
    }

    @Transactional
    @Override
    public void audit(String id) {
        T entity = this.getById(id);
        entity.setOrderStatus(this.getAuditStatus());
        this.fillUpdateInfo(entity);

        this.beforeAudit(entity);
        super.updateById(entity);
        this.afterAudit(entity);
    }

    /**
     * 审核之前需要处理的逻辑
     */
    public void beforeAudit(T entity) {
    }

    /**
     * 审核之后需要处理的逻辑
     */
    public void afterAudit(T entity) {
    }

    @Transactional
    @Override
    public void invalid(String id) {
        T entity = this.getById(id);
        entity.setOrderStatus(this.getInvalidStatus());
        this.fillUpdateInfo(entity);

        this.beforeInvalid(entity);
        super.updateById(entity);
        this.afterInvalid(entity);

    }

    /**
     * 状态流转直接updateById,不走update流程,需手动补修改审计信息
     */
    private void fillUpdateInfo(T entity) {
        entity.setModifiedOn(LocalDateTime.now());
        entity.setModifiedBy(SecurityContextHolder.getCurrentUsername());
    }

    /**
     * 作废之前需要处理的逻辑
     */
    public void beforeInvalid(T entity) {
    }

    /**
     * 作废之后需要处理的逻辑
     */
    public void afterInvalid(T entity) {
    }

    //==================================================================================================================

    /**
     * 生成单据编号
     */
    public abstract String generateOrderNum();

    /**
     * 获得初始状态
     */
    public String getInitialStatus() {
        return "01";
    }

    /**
     * 获得提交状态
     */
    public String getSubmitStatus() {
        return "02";
    }

    /**
     * 获得审核状态
     */
    public String getAuditStatus() {
        return "03";
    }

    /**
     * 获得作废状态
     */
    public String getInvalidStatus() {
        return "06";
    }

}
