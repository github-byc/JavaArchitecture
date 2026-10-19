package com.goldlion.core.framework.excel.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.TypeUtil;
import com.goldlion.core.common.exception.ExcelException;
import com.goldlion.core.framework.crud.service.BaseService;
import com.goldlion.core.framework.excel.ErrorMessage;
import com.goldlion.core.framework.excel.dto.ExcelUploadDTO;

import java.util.Collections;
import java.util.List;

/**
 * Excel导入 服务类
 */
public interface ExcelUploadService<ExcelUpload extends ExcelUploadDTO, DTO> {

    default void setDtoClass(Class<?> clazz) {
    }

    default void setBaseService(BaseService service) {

    }

    /**
     * 逻辑数据校验
     */
    default List<ErrorMessage> checkData(List<ExcelUpload> dataList) {
        return Collections.emptyList();
    }

    /**
     * 过滤数据
     */
    default List<ExcelUpload> filterData(List<ExcelUpload> dataList) {
        return dataList;
    }

    /**
     * 转换数据
     */
    default List<DTO> convertData(List<ExcelUpload> dataList) {
        Class<?> clazz = (Class<?>) TypeUtil.getTypeArgument(this.getClass(), 1);
        return (List<DTO>) BeanUtil.copyToList(dataList, clazz);
    }

    /**
     * 保存数据
     */
    void saveData(List<DTO> dataList);

    /**
     * Excel导入流程
     *
     * @param dataList Excel对象数据
     */
    default void handel(List<ExcelUpload> dataList) {
        // 逻辑数据校验
        List<ErrorMessage> errorMessageList = this.checkData(dataList);
        if (CollUtil.isNotEmpty(errorMessageList)) {
            throw new ExcelException("Excel逻辑数据校验失败！", errorMessageList);
        }
        if (CollUtil.isEmpty(dataList)) {
            throw new ExcelException("请在Excel文件中记录至少一行数据！");
        }

        // 过滤Excel对象数据
        List<ExcelUpload> filterDataList = this.filterData(dataList);

        // Excel对象数据转换为保存对象数据
        List<DTO> convertDataList = this.convertData(filterDataList);

        // 保存数据
        this.saveData(convertDataList);
    }

}
