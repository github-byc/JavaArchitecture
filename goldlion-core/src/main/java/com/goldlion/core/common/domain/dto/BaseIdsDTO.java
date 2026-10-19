package com.goldlion.core.common.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class BaseIdsDTO {
    @NotEmpty(message = "主键不能为空")
    @Size(min = 1, message = "至少需要一个主键")
    private Set<String> ids;
}
