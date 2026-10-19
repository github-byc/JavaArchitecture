package com.goldlion.core.framework.security.authorization;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.goldlion.core.common.domain.model.AuthLoginUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 基础授权管理器
 */
public abstract class BaseAuthorizationManager<PortalUser extends AuthLoginUser, ApiUser extends AuthLoginUser>
        implements AuthorizationManager<RequestAuthorizationContext> {

    protected final static boolean GRANTED = true;
    protected final static boolean NOT_GRANTED = false;
    protected static final String HEADER_TOKEN = "Token";
    protected static final String[] ALLOW_LOGOUT_REQUEST_ACCESS = new String[]{"/logout", "/oauth2/logout"};
    /**
     * 自定义 openapi 身份验证URI
     */
    protected final static String CUSTOM_OPENAPI_AUTHENTICATED = "/openapi/**";

    //==================================================================================================================

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        HttpServletRequest request = context.getRequest();
        // 预检请求放行
        if (Objects.equals(request.getMethod(), HttpMethod.OPTIONS.name())) {
            return new AuthorizationDecision(true);
        }

        boolean granted = isGranted(authentication.get(), request);
        return new AuthorizationDecision(granted);
    }

    private boolean isGranted(Authentication authentication, HttpServletRequest request) {
        return authentication != null && isAuthorized(authentication, request);
    }

    public abstract boolean isAuthorized(Authentication authentication, HttpServletRequest request);

    /**
     * 获取后端登录用户
     */
    public abstract PortalUser getPortalUser(HttpServletRequest request);

    /**
     * 设置后端上下文
     */
    public abstract void setPortalContext(PortalUser portalUser);

    /**
     * 获取API登录用户
     */
    public abstract ApiUser getApiUser(String clientId);

    /**
     * 设置API上下文
     */
    public abstract void setApiContext(ApiUser apiUser);

    public String[] getAllowLogoutRequestAccess() {
        return ALLOW_LOGOUT_REQUEST_ACCESS;
    }

    public String getCustomOpenapiAuthenticated() {
        return CUSTOM_OPENAPI_AUTHENTICATED;
    }

    //==================================================================================================================

    /**
     * API接口权限
     */
    protected boolean apiAuthorization(Authentication authentication, HttpServletRequest request) {
        // 判断token（必须在Authorization中有）
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StrUtil.isBlank(token)) {
            return NOT_GRANTED;
        }

        Object principal = authentication.getPrincipal();

        // 获取 clientId
        String clientId;
        if (principal instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
            clientId = jwt.getAudience().get(0);
        } else {
            clientId = null;
        }
        if (StrUtil.isBlank(clientId)) {
            return NOT_GRANTED;
        }

        ApiUser apiUser = this.getApiUser(clientId);
        if (Objects.isNull(apiUser)) {
            return NOT_GRANTED;
        }
        Set<String> scopes = apiUser.getScopes();
        if (CollUtil.isEmpty(scopes)) {
            return NOT_GRANTED;
        }

        final String path = request.getRequestURI();

        // 判断权限
        PathMatcher pathMatcher = new AntPathMatcher();
        boolean anyMatch = scopes.stream().anyMatch(scope -> pathMatcher.match(scope, path));
        if (anyMatch) {
            this.setApiContext(apiUser);
            return GRANTED;
        }

        return NOT_GRANTED;
    }

    /**
     * 后端接口权限
     */
    protected boolean portalAuthorization(HttpServletRequest request) {
        PortalUser portalUser = this.getPortalUser(request);
        if (Objects.isNull(portalUser)) {
            return NOT_GRANTED;
        }
        Set<String> scopes = portalUser.getScopes();
        if (CollUtil.isEmpty(scopes)) {
            return NOT_GRANTED;
        }
        // 注销接口放行
        Collections.addAll(scopes, this.getAllowLogoutRequestAccess());

        final String path = getPath(request);

        // 判断权限
        PathMatcher pathMatcher = new AntPathMatcher();
        boolean anyMatch = scopes.stream().anyMatch(scope -> pathMatcher.match(scope, path));
        if (anyMatch) {
            this.setPortalContext(portalUser);
            return GRANTED;
        }
        return NOT_GRANTED;
    }

    //==================================================================================================================

    protected static String getPath(HttpServletRequest request) {
        final String path = request.getRequestURI();
        List<String> pathSplit = StrUtil.split(path, StrUtil.SLASH, true, true);
        int pathSize = pathSplit.size();

        // 根据 REST API 规则
        final String tmpPath;
        // GET 为查询
        if (Objects.equals(request.getMethod(), HttpMethod.GET.name()) && pathSize == 2) {
            tmpPath = path + StrUtil.SLASH + HttpMethod.GET.name();
        }
        // POST 为新增
        else if (Objects.equals(request.getMethod(), HttpMethod.POST.name()) && pathSize == 2) {
            tmpPath = path + StrUtil.SLASH + HttpMethod.POST.name();
        }
        // PUT 为更新
        else if (Objects.equals(request.getMethod(), HttpMethod.PUT.name()) && pathSize == 2) {
            tmpPath = path + StrUtil.SLASH + HttpMethod.PUT.name();
        }
        // DELETE 为删除
        else if (Objects.equals(request.getMethod(), HttpMethod.DELETE.name()) && pathSize == 2) {
            tmpPath = path + StrUtil.SLASH + HttpMethod.DELETE.name();
        } else {
            tmpPath = path;
        }

        return tmpPath;
    }

    /**
     * 获取请求token
     */
    protected String getToken(HttpServletRequest request) {
        return request.getHeader(HEADER_TOKEN);
    }

    //==================================================================================================================
}
