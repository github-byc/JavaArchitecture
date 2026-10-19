package com.goldlion.core.framework.security.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.goldlion.core.common.domain.Result;
import com.goldlion.core.framework.security.authorization.BaseAuthorizationManager;
import com.goldlion.core.framework.security.filter.OAuth2TokenFilter;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.IOUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfFilter;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * 权限配置
 */
@RequiredArgsConstructor
public abstract class OAuth2SecurityConfig {
    private static final String[] ALLOW_LOGIN_REQUEST_ACCESS = new String[]{"/login/**", "/oauth2/login"};
    private static final String[] ALLOW_OTHER_REQUEST_ACCESS = new String[]{"/doc.html", "/webjars/**", "/v3/api-docs/**", "/favicon.ico", "/error"};
    private static final String KEYSTORE_NAME = "jwks.json";

    //==================================================================================================================

    private final OAuth2TokenFilter oauth2TokenFilter;
    private final BaseAuthorizationManager authorizationManager;

    private AuthenticationManager authenticationManagerOAuth2;

    //==================================================================================================================

    /**
     * 获取允许登录请求访问
     */
    public String[] getAllowLoginRequestAccess() {
        return ALLOW_LOGIN_REQUEST_ACCESS;
    }

    public String[] getAllowOtherRequestAccess() {
        return ALLOW_OTHER_REQUEST_ACCESS;
    }

    //==================================================================================================================

    /**
     * OAuth2安全配置项
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                // oidc配置
                .oidc(withDefaults());

        http
                // 禁用csrf
                .csrf(CsrfConfigurer::disable)
                // 由于采用token方式认证，因此可以关闭session管理
                .sessionManagement(SessionManagementConfigurer::disable)
                // 禁用原来登录页面
                .formLogin(FormLoginConfigurer::disable)
                // 禁用原有的登出
                .logout(LogoutConfigurer::disable)
                .addFilterAfter(oauth2TokenFilter, CsrfFilter.class)
                // 异常处理
                .exceptionHandling((exceptionHandling) ->
                        exceptionHandling
                                .authenticationEntryPoint(notLoggedInAuthentication())
                                .accessDeniedHandler(noPermissionAccessDenied()))
                // 资源服务
                .oauth2ResourceServer((oauth2ResourceServer) ->
                        oauth2ResourceServer
                                .jwt(Customizer.withDefaults())
                                .authenticationEntryPoint(notLoggedInAuthentication())
                );

        DefaultSecurityFilterChain filterChain = http.build();
        this.authenticationManagerOAuth2 = http.getSharedObject(AuthenticationManager.class);
        return filterChain;
    }

    /**
     * Web安全配置项
     */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {

        http
                // 配置路由拦截
                .authorizeHttpRequests((authorize) ->
                        authorize
                                // 允许登录请求访问
                                .requestMatchers(this.getAllowLoginRequestAccess()).permitAll()
                                // 允许其他请求访问
                                .requestMatchers(this.getAllowOtherRequestAccess()).permitAll()
                                .anyRequest().access(authorizationManager)
                )
                // 禁用csrf
                .csrf(CsrfConfigurer::disable)
                // 由于采用token方式认证，因此可以关闭session管理
                .sessionManagement(SessionManagementConfigurer::disable)
                // 禁用原来登录页面
                .formLogin(FormLoginConfigurer::disable)
                // 禁用原有的登出
                .logout(LogoutConfigurer::disable)
                .addFilterBefore(oauth2TokenFilter, CsrfFilter.class)
                // 异常处理
                .exceptionHandling((exceptionHandling) ->
                        exceptionHandling
                                .authenticationEntryPoint(noPermissionAuthentication())
                                .accessDeniedHandler(noPermissionAccessDenied()))
                // 资源服务
                .oauth2ResourceServer((oauth2ResourceServer) ->
                        oauth2ResourceServer
                                .jwt(Customizer.withDefaults())
                                .authenticationEntryPoint(noPermissionAuthentication())
                );

        return http.build();
    }

    //==================================================================================================================

    /**
     * BCrypt加密
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 提供加密/解密的 source
     * TODO: 请在不同的环境使用不同的key(即更换jwks.json中的文件内容); 如何生成不同的key请参考 JwksTest.java 类.
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() throws IOException, ParseException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(KEYSTORE_NAME)) {
            String keyJson = IOUtils.readInputStreamToString(is, StandardCharsets.UTF_8);
            return new ImmutableJWKSet<>(JWKSet.parse(keyJson));
        }
    }

    /**
     * jwt解码器
     */
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * 默认授权服务器配置
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    //==================================================================================================================

    public AuthenticationManager authenticationManagerOAuth2() {
        return authenticationManagerOAuth2;
    }

    public AccessDeniedHandler noPermissionAccessDenied() {
        return (HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) -> {
            Result result = Result.error(HttpStatus.FORBIDDEN.value(), StrUtil.format("请求访问：{}，认证失败，无访问权限!", request.getRequestURI()));

            response.setContentType("application/json;charset=utf-8");
            try (PrintWriter writer = response.getWriter()) {
                writer.write(JSONUtil.toJsonStr(result));
            }
        };
    }

    public AuthenticationEntryPoint noPermissionAuthentication() {
        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) -> {
            Result result = Result.error(HttpStatus.FORBIDDEN.value(), StrUtil.format("请求访问：{}，认证失败，无访问权限!", request.getRequestURI()));

            response.setContentType("application/json;charset=utf-8");
            try (PrintWriter writer = response.getWriter()) {
                writer.write(JSONUtil.toJsonStr(result));
            }
        };
    }

    public AuthenticationEntryPoint notLoggedInAuthentication() {
        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) -> {
            Result result = Result.error(HttpStatus.UNAUTHORIZED.value(), StrUtil.format("请求访问：{}，认证失败，请重新登录!", request.getRequestURI()));

            response.setContentType("application/json;charset=utf-8");
            try (PrintWriter writer = response.getWriter()) {
                writer.write(JSONUtil.toJsonStr(result));
            }
        };
    }

}