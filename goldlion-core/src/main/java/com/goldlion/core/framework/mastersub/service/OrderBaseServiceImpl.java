package com.goldlion.core.framework.mastersub.service;

import com.goldlion.core.framework.crud.dao.BaseDao;
import com.goldlion.core.framework.crud.dto.QueryDTO;
import com.goldlion.core.framework.crud.service.MybatisPlusServiceImpl;
import com.goldlion.core.framework.mastersub.dto.OrderBaseDTO;
import com.goldlion.core.framework.mastersub.entity.OrderBaseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 单据主子表-主表基础服务类
 */
public abstract class OrderBaseServiceImpl<M extends BaseDao<T>, T extends OrderBaseEntity, BDTO extends OrderBaseDTO, QDTO extends QueryDTO>
        extends MybatisPlusServiceImpl<M, T, BDTO, QDTO>
        implements OrderBaseService<T, BDTO, QDTO> {

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

        this.beforeInvalid(entity);
        super.updateById(entity);
        this.afterInvalid(entity);

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

    @Override
    public void beforeCreate(T entity) {

    }

    @Override
    public void afterCreate(T entity) {

    }

    @Override
    public void beforeCreate(List<T> es) {

    }

    @Override
    public void afterCreate(List<T> es) {

    }

    @Override
    public void beforeUpdate(T entity) {

    }

    @Override
    public void afterUpdate(T entity) {

    }

}
