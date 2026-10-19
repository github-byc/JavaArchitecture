package com.goldlion.core.framework.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.goldlion.core.framework.crud.dao.BaseDao;
import com.goldlion.core.framework.mastersub.dto.*;
import com.goldlion.core.framework.mastersub.entity.OrderBaseEntity;
import com.goldlion.core.framework.mastersub.entity.SubBaseEntity;
import com.goldlion.core.framework.mastersub.service.*;
import com.goldlion.core.framework.mastersub.vo.OrderBaseVO;
import com.goldlion.core.framework.mastersub.vo.SubBaseVO;
import com.goldlion.core.framework.mastersub.web.OrderExcelController;
import com.goldlion.core.framework.mastersub.web.SubExcelController;

/**
 * 主子表代码生成器实现
 */
public abstract class OrderTableGeneratorImpl extends BaseGeneratorImpl {

    @Override
    public void execute() {
        FastAutoGenerator masterGenerator = this.init();

        String author = masterGenerator.scannerNext("请输入作者名称");
        String masterTableName = masterGenerator.scannerNext("请输入单据主表名");
        String subTableName = masterGenerator.scannerNext("请输入单据子表名，多个表名用,隔开");

        this.executeMaster(masterGenerator, author, masterTableName);

        FastAutoGenerator subGenerator = this.init();
        this.executeSub(subGenerator, author, subTableName);
    }

    //==================================================================================================================

    /**
     * 生成子表
     */
    public void executeSub(FastAutoGenerator fastAutoGenerator, String author, String tableName) {
        // 策略配置
        this.setStrategyConfig(
                fastAutoGenerator,
                tableName,
                SubBaseEntity.class,
                BaseDao.class,
                SubBaseService.class,
                SubBaseServiceImpl.class,
                SubExcelController.class);

        // 注入配置
        this.setInjectionConfig(
                fastAutoGenerator,
                SubBaseDTO.class,
                SubExcelQueryDTO.class,
                SubExcelUploadDTO.class,
                SubBaseVO.class);

        this.execute(fastAutoGenerator, author);
    }

    /**
     * 生成主表
     */
    public void executeMaster(FastAutoGenerator fastAutoGenerator, String author, String tableName) {
        // 策略配置
        this.setStrategyConfig(
                fastAutoGenerator,
                tableName,
                OrderBaseEntity.class,
                BaseDao.class,
                OrderBaseService.class,
                OrderBaseServiceImpl.class,
                OrderExcelController.class);

        // 注入配置
        this.setInjectionConfig(
                fastAutoGenerator,
                OrderBaseDTO.class,
                OrderExcelQueryDTO.class,
                OrderExcelUploadDTO.class,
                OrderBaseVO.class);

        this.execute(fastAutoGenerator, author);
    }

    public void execute(FastAutoGenerator fastAutoGenerator, String author) {
        // 全局配置
        this.setGlobalConfig(fastAutoGenerator, author);
        // 包配置
        this.setPackageConfig(fastAutoGenerator);

        fastAutoGenerator
                // 使用Freemarker引擎模板
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }

    //==================================================================================================================

}
