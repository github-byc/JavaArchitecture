package com.goldlion.core.framework.security.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Schema(description = "注册客户端")
public class OAuth2Client implements java.io.Serializable {

    @NotBlank(message = "客户端名称不能为空")
    @Schema(description = "客户端名称")
    private String clientName;

    @NotBlank(message = "客户端回调地址不能为空")
    @Schema(description = "客户端回调地址")
    private String redirectUri;

    @NotBlank(message = "客户端首页地址不能为空")
    @Schema(description = "客户端首页地址")
    private String postLogoutRedirectUri;

    @Schema(description = "客户端id")
    private String clientId;

    @Schema(description = "客户端密钥")
    private String clientSecret;

    @Schema(description = "客户端权限")
    private Set<String> scopes;

}
