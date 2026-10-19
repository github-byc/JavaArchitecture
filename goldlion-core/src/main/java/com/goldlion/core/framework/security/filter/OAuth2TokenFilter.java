package com.goldlion.core.framework.security.filter;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * OAuth2令牌过滤器
 */
public abstract class OAuth2TokenFilter<OAuth2User extends UserDetails> extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!"/oauth2/authorize".equals(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 从请求获取token:优先Authorization Bearer头(不进访问日志),其次access_token参数(兼容存量前端,会进访问日志,逐步淘汰)
        String token = this.resolveToken(request);
        if (StrUtil.isBlank(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        OAuth2User oAuth2User = this.getOAuth2User(token);
        if (Objects.isNull(oAuth2User)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 将用户信息存入 SecurityContextHolder
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(oAuth2User, null, null);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        filterChain.doFilter(request, response);
    }

    /**
     * 获取OAuth2登录用户
     */
    public abstract OAuth2User getOAuth2User(String token);

    /**
     * 解析token:优先Authorization Bearer头(不进访问日志),其次access_token参数(GET查询串/POST表单均覆盖)
     */
    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StrUtil.startWithIgnoreCase(authorization, "Bearer ")) {
            return authorization.substring(7).trim();
        }
        return request.getParameter("access_token");
    }

    static MultiValueMap<String, String> getQueryParameters(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameterMap.forEach((key, values) -> {
            String queryString = StringUtils.hasText(request.getQueryString()) ? request.getQueryString() : "";
            if (queryString.contains(key)) {
                for (String value : values) {
                    parameters.add(key, value);
                }
            }
        });
        return parameters;
    }
}
