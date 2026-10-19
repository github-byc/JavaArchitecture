package com.goldlion.core.framework.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.goldlion.core.framework.crud.dao.BaseDao;
import com.goldlion.core.framework.crud.dto.BaseDTO;
import com.goldlion.core.framework.crud.entity.BaseEntity;
import com.goldlion.core.framework.crud.service.BaseService;
import com.goldlion.core.framework.crud.service.RedisCacheServiceImpl;
import com.goldlion.core.framework.crud.vo.BaseVO;
import com.goldlion.core.framework.excel.dto.ExcelQueryDTO;
import com.goldlion.core.framework.excel.dto.ExcelUploadDTO;
import com.goldlion.core.framework.excel.web.ExcelController;

/**
 * 单表代码生成器实现
 */
public abstract class SingleTableGeneratorImpl extends BaseGeneratorImpl {

    @Override
    public void execute() {
        // 初始化数据库脚本
        FastAutoGenerator fastAutoGenerator = this.init();

        String author = fastAutoGenerator.scannerNext("请输入作者名称");
        String tableName = fastAutoGenerator.scannerNext("请输入表名，多个表名用,隔开");

        execute(fastAutoGenerator, author, tableName);
    }

    public void execute(FastAutoGenerator fastAutoGenerator, String author, String tableName) {
        // 全局配置
        this.setGlobalConfig(fastAutoGenerator, author);

        // 包配置
        this.setPackageConfig(fastAutoGenerator);

        // 策略配置
        this.setStrategyConfig(
                fastAutoGenerator,
                tableName,
                this.getSuperEntityClass(),
                BaseDao.class,
                this.getSuperServiceClass(),
                this.getSuperServiceImplClass(),
                this.getSuperControllerClass());

        // 注入配置
        this.setInjectionConfig(
                fastAutoGenerator,
                this.getSuperDTOClass(),
                this.getSuperQueryDTOClass(),
                this.getSuperUploadDTOClass(),
                this.getSuperVOClass());

        fastAutoGenerator
                // 使用Freemarker引擎模板
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }

    //==================================================================================================================

    /**
     * 实体父类
     */
    public Class<?> getSuperEntityClass() {
        return BaseEntity.class;
    }

    /**
     * 服务父类
     */
    public Class<?> getSuperServiceClass() {
        return BaseService.class;
    }

    /**
     * 服务实现父类
     */
    public Class<?> getSuperServiceImplClass() {
        return RedisCacheServiceImpl.class;
    }

    /**
     * 前端控制器父类
     */
    public Class<?> getSuperControllerClass() {
        return ExcelController.class;
    }

    /**
     * DTO父类
     */
    public Class<?> getSuperDTOClass() {
        return BaseDTO.class;
    }

    /**
     * 查询条件父类
     */
    public Class<?> getSuperQueryDTOClass() {
        return ExcelQueryDTO.class;
    }

    /**
     * Excel父类
     */
    public Class<?> getSuperUploadDTOClass() {
        return ExcelUploadDTO.class;
    }

    /**
     * VO父类
     */
    public Class<?> getSuperVOClass() {
        return BaseVO.class;
    }

}
