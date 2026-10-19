package com.goldlion.core.framework.security.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OAuth2用户")
// 不加 @JsonTypeInfo 注解会报错误：is not in the allowlist. If you believe this class is safe to deserialize, please provide an explicit mapping using Jackson annotations or by providing a Mixin. If the serialization is only done by a trusted source, you can also enable default typing.
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuth2User implements UserDetails {

    @Schema(description = "用户Id")
    private String userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户密码")
    private String password;

    @Schema(description = "中文名")
    private String chineseName;

    @Schema(description = "座机号")
    private String fixedPhone;

    @Schema(description = "手机号")
    private String mobilePhone;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "电子邮件")
    private String email;

    @Schema(description = "令牌")
    private String token;

    public OAuth2User(String userId, String username, String password) {
        this.userId = Objects.requireNonNull(userId, "userId");
        this.username = Objects.requireNonNull(username, "username");
        this.password = Objects.requireNonNull(password, "password");
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }
}
