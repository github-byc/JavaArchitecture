package com.goldlion.core.common.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseIdDTO {
    @NotBlank(message = "主键不能为空")
    private String id;
}
