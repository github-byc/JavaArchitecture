package com.goldlion.core.framework.crud.dto;

import com.goldlion.core.common.validation.UpdateGroup;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 基础DTO
 */
@Getter
@Setter
public class BaseDTO {
    @NotBlank(message = "主键不能为空", groups = {UpdateGroup.class})
    private String id;
}
