package com.goldlion.core.framework.excel.web;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.idev.excel.EasyExcel;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.FastExcel;
import cn.idev.excel.write.builder.ExcelWriterBuilder;
import cn.idev.excel.write.builder.ExcelWriterSheetBuilder;
import cn.idev.excel.write.metadata.WriteSheet;
import com.goldlion.core.common.exception.ExcelException;
import com.goldlion.core.framework.crud.dto.BaseDTO;
import com.goldlion.core.framework.crud.entity.BaseEntity;
import com.goldlion.core.framework.crud.service.BaseService;
import com.goldlion.core.framework.crud.vo.BaseVO;
import com.goldlion.core.framework.crud.web.CrudController;
import com.goldlion.core.framework.excel.CommentWriteHandler;
import com.goldlion.core.framework.excel.ErrorMessage;
import com.goldlion.core.framework.excel.dto.ExcelQueryDTO;
import com.goldlion.core.framework.excel.dto.ExcelUploadDTO;
import com.goldlion.core.framework.excel.listener.DefaultUploadDataListener;
import com.goldlion.core.framework.excel.listener.ListAnalysisEventListener;
import com.goldlion.core.framework.excel.service.ExcelUploadService;
import com.goldlion.core.framework.excel.service.ExcelUploadServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Excel控制器
 */
public abstract class ExcelController<
        BS extends BaseService<? extends BaseEntity, BDTO, EQDTO>,
        BDTO extends BaseDTO,
        EQDTO extends ExcelQueryDTO,
        VO extends BaseVO,
        ExcelUpload extends ExcelUploadDTO>
        extends CrudController<BS, BDTO, EQDTO, VO> {

    private final Class<ExcelUpload> euClass;

    public ExcelController() {
        Type[] types = TypeUtil.getTypeArguments(this.getClass());
        this.euClass = (Class<ExcelUpload>) types[4];
    }

    /**
     * Excel导出
     *
     * @see <a href="https://idev.cn/fastexcel/zh-CN/docs/write/write_spring">SpringBoot写入</a>
     */
    @Operation(summary = "Excel导出")
    @PostMapping("/excel/download")
    public void download(HttpServletResponse response, @RequestBody @Validated EQDTO dto) {
        ExcelQueryDTO.ExcelCondition excelCondition = dto.getExcelCondition();
        String formatFileName = StrUtil.format("{}_导出列表_{}", StrUtil.isNotBlank(excelCondition.getFileName()) ? excelCondition.getFileName() : "", DateUtil.current());
        excelWriter(response, formatFileName, excelCondition, this.voClass, () -> this.search(dto));
    }

    @Operation(summary = "Excel导入模板")
    @PostMapping("/excel/template")
    public void template(HttpServletResponse response, @RequestBody EQDTO dto) {
        ExcelQueryDTO.ExcelCondition excelCondition = dto.getExcelCondition();
        String formatFileName = StrUtil.format("{}_导入模板_{}", StrUtil.isNotBlank(excelCondition.getFileName()) ? excelCondition.getFileName() : "", DateUtil.current());
        excelWriter(response, formatFileName, excelCondition, this.getExcelUploadClass(), Collections::emptyList);
    }

    /**
     * Excel导入
     *
     * @see <a href="https://idev.cn/fastexcel/zh-CN/docs/read/read_spring">SpringBoot读取</a>
     */
    @Operation(summary = "Excel导入")
    @PostMapping("/excel/upload")
    public void upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new ExcelException("请选择一个文件上传！");
        }

        Class<? extends ExcelUploadDTO> excelUploadClass = this.getExcelUploadClass();

        ListAnalysisEventListener<?> readListener;
        ExcelUploadService<?, ?> excelUploadService;
        try {
            Class<? extends ListAnalysisEventListener<?>> readListenerClass = this.getReadListenerClass();
            readListener = BeanUtils.instantiateClass(readListenerClass);

            Class<? extends ExcelUploadService<?, ?>> excelUploadServiceClass = this.getExcelUploadServiceClass();
            excelUploadService = SpringUtil.getBean(excelUploadServiceClass);

            FastExcel.read(file.getInputStream(), excelUploadClass, readListener).sheet().doRead();
        } catch (ExcelException e) {
            throw new ExcelException(e.getMessage(), e);
        } catch (Exception e) {
            throw new ExcelException("Excel文件处理失败！", e);
        }

        // 基本数据校验
        List<ErrorMessage> errorMessageList = readListener.getErrors();
        if (CollUtil.isNotEmpty(errorMessageList)) {
            throw new ExcelException("Excel基本数据校验失败！", errorMessageList);
        }

        // 读取时已按excelUploadClass解析,直接使用即可,不再整表复制一份(降低大文件内存峰值)
        List excelUploadList = readListener.getList();
        excelUploadService.setDtoClass(this.dtoClass);
        excelUploadService.setBaseService(this.baseService);
        excelUploadService.handel(excelUploadList);
    }

    //==================================================================================================================

    /**
     * 导入Excel服务实现类
     * </p>
     * 子类如需要使用请覆盖该方法
     */
    protected Class<? extends ExcelUploadService<?, ?>> getExcelUploadServiceClass() {
        return ExcelUploadServiceImpl.class;
    }

    /**
     * 导入ExcelDTO
     * </p>
     * 子类如需要使用请覆盖该方法
     */
    protected Class<? extends ExcelUploadDTO> getExcelUploadClass() {
        return this.euClass;
    }

    /**
     * ListAnalysisEventListener
     * </p>
     * 子类如需要使用请覆盖该方法
     */
    private Class<? extends ListAnalysisEventListener<?>> getReadListenerClass() {
        return DefaultUploadDataListener.class;
    }

    //==================================================================================================================

    private void excelWriter(HttpServletResponse response, String formatFileName, ExcelQueryDTO.ExcelCondition excelCondition, Class<?> clazz, Supplier<List<VO>> getDataList) {
        String fileName = URLEncoder.encode(formatFileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        List<VO> voList = getDataList.get();

        ExcelWriter excelWriter = this.getExcelWriter(response, clazz, excelCondition, CollUtil.isEmpty(voList));
        WriteSheet writeSheet = this.sheet(excelCondition);

        // 写入sheet
        excelWriter.write(voList, writeSheet);
        excelWriter.finish();
    }

    @SneakyThrows(IOException.class)
    private ExcelWriter getExcelWriter(HttpServletResponse response, Class<?> clazz, ExcelQueryDTO.ExcelCondition condition, boolean isData) {
        ExcelWriterBuilder writerBuilder = EasyExcel.write(response.getOutputStream()).autoCloseStream(true).head(clazz);


        if (CollUtil.isNotEmpty(condition.getIncludeColumns())) {
            writerBuilder.includeColumnFieldNames(condition.getIncludeColumns());
        }
        if (CollUtil.isNotEmpty(condition.getExcludeColumns())) {
            writerBuilder.excludeColumnFieldNames(condition.getExcludeColumns());
        }

        if (isData) {
            // 批注必须启用内存模式
            writerBuilder.inMemory(Boolean.TRUE)
                    .registerWriteHandler(new CommentWriteHandler(clazz));
        }

        return writerBuilder.build();
    }

    private WriteSheet sheet(ExcelQueryDTO.ExcelCondition condition) {
        ExcelWriterSheetBuilder writerSheetBuilder = EasyExcel.writerSheet();
        return writerSheetBuilder.build();
    }

}
