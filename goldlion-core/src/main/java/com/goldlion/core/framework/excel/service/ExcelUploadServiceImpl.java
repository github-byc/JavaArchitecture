package com.goldlion.core.framework.excel.service;

import cn.hutool.core.bean.BeanUtil;
import com.goldlion.core.framework.crud.service.BaseService;
import com.goldlion.core.framework.excel.dto.ExcelUploadDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Excel导入 服务类默认实现
 */
@Service
public class ExcelUploadServiceImpl implements ExcelUploadService<ExcelUploadDTO, Object> {

    private Class<?> dtoClass;
    private BaseService service;

    @Override
    public void setDtoClass(Class<?> clazz) {
        this.dtoClass = clazz;
    }

    @Override
    public void setBaseService(BaseService service) {
        this.service = service;
    }

    @Override
    public List<Object> convertData(List<ExcelUploadDTO> dataList) {
        return (List<Object>) BeanUtil.copyToList(dataList, this.dtoClass);
    }

    @Override
    public void saveData(List<Object> dataList) {
        this.service.batchCreate(dataList);
    }

}
