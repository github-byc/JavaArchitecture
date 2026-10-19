package com.goldlion.core.fixture;

import com.goldlion.core.framework.excel.dto.ExcelUploadDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 单元测试专用Excel上传DTO
 */
@Getter
@Setter
public class TestUploadDTO extends ExcelUploadDTO {

    @NotBlank(message = "名称不能为空")
    private String name;

}
