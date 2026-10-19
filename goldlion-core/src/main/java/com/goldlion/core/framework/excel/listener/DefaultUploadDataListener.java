package com.goldlion.core.framework.excel.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.log.StaticLog;
import cn.idev.excel.context.AnalysisContext;
import com.goldlion.core.common.exception.ExcelException;
import com.goldlion.core.framework.excel.ErrorMessage;
import com.goldlion.core.framework.excel.Validators;
import com.goldlion.core.framework.excel.dto.ExcelUploadDTO;
import jakarta.validation.ConstraintViolation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 默认上传数据侦听器
 */
public class DefaultUploadDataListener extends ListAnalysisEventListener<ExcelUploadDTO> {
    /**
     * 默认最大行数,防止大文件全量读入内存导致OOM,可通过goldlion.excel.max-rows调整
     */
    private static final int DEFAULT_MAX_ROWS = 20000;

    private final List<ExcelUploadDTO> dataList = new ArrayList<>();
    private final List<ErrorMessage> errorMessageList = new ArrayList<>();
    private volatile Integer maxRows;

    @Override
    public void invoke(ExcelUploadDTO data, AnalysisContext analysisContext) {
        Integer rowIndex = analysisContext.readRowHolder().getRowIndex();
        Long lineNum = rowIndex + 1L;

        // 行数上限保护:超过即终止导入,请分批导入
        if (this.dataList.size() + this.errorMessageList.size() >= this.getMaxRows()) {
            throw new ExcelException(StrUtil.format("Excel数据超过最大行数限制:{}行,请分批导入!", this.getMaxRows()));
        }

        Set<ConstraintViolation<Object>> violations = Validators.validate(data);
        if (CollUtil.isNotEmpty(violations)) {
            Set<String> messageSet = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.toSet());
            errorMessageList.add(new ErrorMessage(lineNum, messageSet));
        } else {
            data.setLineNum(lineNum);
            dataList.add(data);
        }
    }

    /**
     * 最大行数,子类可覆写
     */
    protected int getMaxRows() {
        if (this.maxRows == null) {
            try {
                this.maxRows = Convert.toInt(SpringUtil.getProperty("goldlion.excel.max-rows"), DEFAULT_MAX_ROWS);
            } catch (Exception e) {
                // Spring上下文未就绪时使用默认值
                this.maxRows = DEFAULT_MAX_ROWS;
            }
        }
        return this.maxRows;
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        StaticLog.info("Excel读取完成，数据大小:{}", dataList.size());
    }

    @Override
    public List<ExcelUploadDTO> getList() {
        return this.dataList;
    }

    @Override
    public List<ErrorMessage> getErrors() {
        return this.errorMessageList;
    }
}
