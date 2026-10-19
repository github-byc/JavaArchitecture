package com.goldlion.core.framework.generator;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.builder.CustomFile;
import com.baomidou.mybatisplus.generator.util.ClassUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 基础代码生成器实现
 */
public abstract class BaseGeneratorImpl implements BaseGenerator {

    /**
     * 初始化
     */
    FastAutoGenerator init() {
        return FastAutoGenerator.create(this.initDataSource());
    }

    //==================================================================================================================

    public abstract String getDataSourceUrl();

    public abstract String getDataSourceSchemaName();

    public abstract String getDataSourceUsername();

    public abstract String getDataSourcePassword();

    /**
     * 数据源配置
     */
    DataSourceConfig.Builder initDataSource() {
        return new DataSourceConfig
                .Builder("jdbc:mysql://" + getDataSourceUrl() + "/" + getDataSourceSchemaName() + "?serverTimezone=Asia/Shanghai", getDataSourceUsername(), getDataSourcePassword())
                .schema(getDataSourceSchemaName());
    }

    //==================================================================================================================

    /**
     * 全局配置
     *
     * @param fastAutoGenerator 代码生成器
     * @param author            作者名
     */
    void setGlobalConfig(FastAutoGenerator fastAutoGenerator, String author) {
        fastAutoGenerator.globalConfig(builder -> builder
                // 设置作者名
                .author(author)
                // 开启 Swagger 模式
                .enableSpringdoc());
    }

    //==================================================================================================================

    /**
     * 父包名
     */
    public abstract String getParentPackage();

    /**
     * 父包模块名
     */
    public abstract String getModuleName();

    /**
     * 包配置
     *
     * @param fastAutoGenerator 代码生成器
     */
    void setPackageConfig(FastAutoGenerator fastAutoGenerator) {
        fastAutoGenerator.packageConfig(builder -> builder
                // 设置父包名
                .parent(getParentPackage())
                // 设置父包模块名
                .moduleName(getModuleName())
                // 设置 Entity 包名
                .entity("model")
                // 设置 Service 包名
                .service("service")
                // 设置 Service Impl 包名
                .serviceImpl("service.impl")
                // 设置 Mapper 包名
                .mapper("dao")
                // 设置 Mapper XML 包名
                .xml("mappers")
                // 设置 Controller 包名
                .controller("controller"));
    }

    //==================================================================================================================

    public abstract List<String> getTablePrefixList();

    protected List<String> getDefaultTablePrefix() {
        return Arrays.asList(CollUtil.getLast(StrUtil.split(getParentPackage(), ".")));
    }

    /**
     * 策略配置
     *
     * @param fastAutoGenerator     代码生成器
     * @param tableName             表名
     * @param superEntityClass      实体父类
     * @param superMapperClass      映射器父类
     * @param superServiceClass     服务父类
     * @param superServiceImplClass 服务实现父类
     * @param superControllerClass  前端控制器父类
     */
    void setStrategyConfig(FastAutoGenerator fastAutoGenerator, String tableName,
                           Class<?> superEntityClass,
                           Class<?> superMapperClass,
                           Class<?> superServiceClass,
                           Class<?> superServiceImplClass,
                           Class<?> superControllerClass) {

        fastAutoGenerator.strategyConfig(builder -> builder
                .addInclude(tableName)
                // 过滤表前缀
                .addTablePrefix(getTablePrefixList())
                // Entity 策略配置
                .entityBuilder()
                .versionColumnName("version")
                .versionPropertyName("version")
                // 禁用生成 serialVersionUID
                .disableSerialVersionUID()
                // 开启 Lombok 模型
                .enableLombok()
                // 开启 Boolean 类型字段移除 is 前缀
                .enableRemoveIsPrefix()
                // 开启生成实体时生成字段注解
                .enableTableFieldAnnotation()
                // 设置父类
                .superClass(superEntityClass)
                // Mapper 策略配置
                .mapperBuilder()
                .superClass(superMapperClass)
                .enableMapperAnnotation()
                .disableMapperXml()
                // Service 策略配置
                .serviceBuilder()
                // 设置接口父类
                .superServiceClass(superServiceClass)
                // 设置实现类父类
                .superServiceImplClass(superServiceImplClass)
                // 设置接口模板
                .serviceTemplate("templates/Service.java")
                // 设置实现类模板
                .serviceImplTemplate("templates/ServiceImpl.java")
                // Controller 策略配置
                .controllerBuilder()
                // 开启生成@RestController 控制器
                .enableRestStyle()
                // 设置父类
                .superClass(superControllerClass)
                // 设置模板
                .template("templates/Controller.java"));
    }

    //==================================================================================================================

    protected abstract Map<String, Object> getCustomMap();

    /**
     * 注入配置
     *
     * @param fastAutoGenerator   代码生成器
     * @param superDTOClass       DTO父类
     * @param superQueryDTOClass  查询条件父类
     * @param superUploadDTOClass Excel父类
     * @param superVOClass        VO父类
     */
    void setInjectionConfig(FastAutoGenerator fastAutoGenerator,
                            Class<?> superDTOClass,
                            Class<?> superQueryDTOClass,
                            Class<?> superUploadDTOClass,
                            Class<?> superVOClass) {

        fastAutoGenerator.injectionConfig((scanner, builder) -> {
            Map<String, Object> customMap = new HashMap<>();
            customMap.put("parentModuleName", CollUtil.getLast(StrUtil.split(getParentPackage(), ".")));

            Map<String, Object> subCustomMap = getCustomMap();
            if (subCustomMap != null && !subCustomMap.isEmpty()) {
                customMap.putAll(subCustomMap);
            }

            setSuperDTOClass(customMap, superDTOClass);
            setSuperQueryDTOClass(customMap, superQueryDTOClass);
            setSuperUploadDTOClass(customMap, superUploadDTOClass);
            setSuperVOClass(customMap, superVOClass);

            builder.customMap(customMap); //注入自定义属性

            final String dtoPackageName = "dto";
            final String excelPackageName = "dto.excel";
            final String voPackageName = "vo";

            builder
                    // 自定义DTO
                    .customFile(new CustomFile.Builder()
                            .packageName(dtoPackageName)
                            .fileName("DTO.java")
                            .templatePath("templates/DTO.java.ftl")
                            .build())
                    // 自定义QueryDTO
                    .customFile(new CustomFile.Builder()
                            .packageName(dtoPackageName)
                            .fileName("QueryDTO.java")
                            .templatePath("templates/QueryDTO.java.ftl")
                            .build())
                    // 自定义Excel导入DTO
                    .customFile(new CustomFile.Builder()
                            .packageName(excelPackageName)
                            .fileName("UploadDTO.java")
                            .templatePath("templates/UploadDTO.java.ftl")
                            .build())
                    // 自定义VO
                    .customFile(new CustomFile.Builder()
                            .packageName(voPackageName)
                            .fileName("VO.java")
                            .templatePath("templates/VO.java.ftl")
                            .build())
            ;

            this.injectionConfig(scanner, builder);
        });
    }

    public void injectionConfig(Function<String, String> scanner, InjectionConfig.Builder builder) {
    }

    //==================================================================================================================

    private void setSuperVOClass(Map<String, Object> customMap, Class<?> superVOClass) {
        String superClass = superVOClass.getName();
        customMap.put("superVOClassPackage", StringUtils.isBlank(superClass) ? null : superClass);
        customMap.put("superVOClass", ClassUtils.getSimpleName(superClass));
    }

    private void setSuperUploadDTOClass(Map<String, Object> customMap, Class<?> superUploadDTOClass) {
        String superClass = superUploadDTOClass.getName();
        customMap.put("superUploadDTOClassPackage", StringUtils.isBlank(superClass) ? null : superClass);
        customMap.put("superUploadDTOClass", ClassUtils.getSimpleName(superClass));
    }

    private void setSuperQueryDTOClass(Map<String, Object> customMap, Class<?> superQueryDTOClass) {
        String superClass = superQueryDTOClass.getName();
        customMap.put("superQueryDTOClassPackage", StringUtils.isBlank(superClass) ? null : superClass);
        customMap.put("superQueryDTOClass", ClassUtils.getSimpleName(superClass));
    }

    private void setSuperDTOClass(Map<String, Object> customMap, Class<?> superDTOClass) {
        String superClass = superDTOClass.getName();
        customMap.put("superDTOClassPackage", StringUtils.isBlank(superClass) ? null : superClass);
        customMap.put("superDTOClass", ClassUtils.getSimpleName(superClass));
    }

    //==================================================================================================================

}
