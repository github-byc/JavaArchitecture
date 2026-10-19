package com.goldlion.core.framework.security.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.log.StaticLog;
import com.goldlion.core.common.exception.BusinessException;
import com.goldlion.core.framework.security.config.OAuth2SecurityConfig;
import com.goldlion.core.framework.security.domain.model.OAuth2User;
import com.goldlion.core.framework.security.domain.rediskey.LoginDataCacheKey;
import com.goldlion.core.framework.security.service.OAuth2LoginService;
import com.goldlion.core.middleware.redis.RedisOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * 基础OAuth2登录 服务实现
 */
@RequiredArgsConstructor
@Service
public class BaseOAuth2LoginServiceImpl implements OAuth2LoginService {
    private final RedisOperator redisOperator;

    //==================================================================================================================

    private AuthenticationManager authenticationManager;

    @Override
    public String login(String username, String password) {
        UsernamePasswordAuthenticationToken authenticationToken = UsernamePasswordAuthenticationToken.unauthenticated(username, password);
        try {
            checkAndInitialAuthenticationManager();

            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            if (authentication != null && authentication.isAuthenticated()) {
                SecurityContextHolder.getContext().setAuthentication(authentication);

                OAuth2User user = (OAuth2User) authentication.getPrincipal();
                user.setToken(IdUtil.objectId());

                redisOperator.setValue(LoginDataCacheKey.OAUTH2_LOGIN_TOKEN, user.getToken(), user);
                return user.getToken();
            } else {
                throw new BusinessException("用户密码输入错误,请重新输入!");
            }
        } catch (Exception e) {
            throw new BusinessException("用户密码输入错误,请重新输入!");
        }
    }

    @Override
    public void logout() {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            OAuth2User user = (OAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (Objects.nonNull(user) && StrUtil.isNotBlank(user.getToken())) {
                redisOperator.deleteValue(LoginDataCacheKey.OAUTH2_LOGIN_TOKEN, user.getToken());
            } else {
                throw new BusinessException("登出失败，用户不存在");
            }
        }
    }

    @Override
    public OAuth2User getOAuth2User(String token) {
        if (StrUtil.isNotEmpty(token)) {
            try {
                Object value = redisOperator.getValue(LoginDataCacheKey.OAUTH2_LOGIN_TOKEN, token);
                return BeanUtil.toBean(value, OAuth2User.class);
            } catch (Exception e) {
                StaticLog.error("获取用户信息异常'{}'", e.getMessage());
            }
        }
        return null;
    }

    //==================================================================================================================

    private void checkAndInitialAuthenticationManager() {
        if (this.authenticationManager == null) {
            OAuth2SecurityConfig securityConfig = SpringUtil.getBean(OAuth2SecurityConfig.class);
            this.authenticationManager = securityConfig.authenticationManagerOAuth2();
            Assert.notNull(this.authenticationManager, "authenticationManager cannot be null");
        }
    }
}
