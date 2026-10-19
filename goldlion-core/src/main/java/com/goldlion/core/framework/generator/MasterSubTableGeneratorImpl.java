package com.goldlion.core.framework.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.goldlion.core.framework.crud.dao.BaseDao;
import com.goldlion.core.framework.mastersub.dto.*;
import com.goldlion.core.framework.mastersub.entity.MasterBaseEntity;
import com.goldlion.core.framework.mastersub.entity.SubBaseEntity;
import com.goldlion.core.framework.mastersub.service.MasterBaseService;
import com.goldlion.core.framework.mastersub.service.MasterBaseServiceImpl;
import com.goldlion.core.framework.mastersub.service.SubBaseService;
import com.goldlion.core.framework.mastersub.service.SubBaseServiceImpl;
import com.goldlion.core.framework.mastersub.vo.MasterBaseVO;
import com.goldlion.core.framework.mastersub.vo.SubBaseVO;
import com.goldlion.core.framework.mastersub.web.MasterExcelController;
import com.goldlion.core.framework.mastersub.web.SubExcelController;

/**
 * 主子表代码生成器实现
 */
public abstract class MasterSubTableGeneratorImpl extends BaseGeneratorImpl {

    @Override
    public void execute() {
        FastAutoGenerator masterGenerator = this.init();

        String author = masterGenerator.scannerNext("请输入作者名称");
        String masterTableName = masterGenerator.scannerNext("请输入主表名");
        String subTableName = masterGenerator.scannerNext("请输入子表名，多个表名用,隔开");

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
                MasterBaseEntity.class,
                BaseDao.class,
                MasterBaseService.class,
                MasterBaseServiceImpl.class,
                MasterExcelController.class);

        // 注入配置
        this.setInjectionConfig(
                fastAutoGenerator,
                MasterBaseDTO.class,
                MasterExcelQueryDTO.class,
                MasterExcelUploadDTO.class,
                MasterBaseVO.class);

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
