package com.goldlion.core.framework.security.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.IdUtil;
import com.goldlion.core.common.function.Tuple2;
import com.goldlion.core.framework.security.domain.model.OAuth2Client;
import com.goldlion.core.framework.security.domain.utils.PasswordUtils;
import com.goldlion.core.framework.security.service.OAuth2ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/**
 * 基础OAuth2客户端 服务实现
 */
@RequiredArgsConstructor
@Service
public class BaseOAuth2ClientServiceImpl implements OAuth2ClientService {
    private final RegisteredClientRepository registeredClientRepository;

    //==================================================================================================================

    @Override
    public OAuth2Client create(OAuth2Client client) {
        Tuple2<String, String> clientSecretTuple2 = PasswordUtils.clientSecret();

        RegisteredClient registeredClient = initClient(client, clientSecretTuple2);
        this.registeredClientRepository.save(registeredClient);

        return toClient(registeredClient, clientSecretTuple2, false);
    }

    @Override
    public OAuth2Client update(OAuth2Client client) {
        RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(client.getClientId());
        if (Objects.isNull(registeredClient)) {
            return null;
        }

        RegisteredClient.Builder builder = RegisteredClient
                .from(registeredClient)
                .clientName(client.getClientName())
                .redirectUri(client.getRedirectUri())
                .postLogoutRedirectUri(client.getPostLogoutRedirectUri());

        if (CollectionUtil.isNotEmpty(client.getScopes())) {
            builder.scopes(scopes -> {
                scopes.clear();
                scopes.add(OidcScopes.OPENID);
                scopes.addAll(client.getScopes());
            });
        } else {
            builder.scopes(scopes -> {
                scopes.clear();
                scopes.add(OidcScopes.OPENID);
            });
        }

        RegisteredClient newRegisteredClient = builder.build();
        this.registeredClientRepository.save(newRegisteredClient);

        return toClient(newRegisteredClient, null, true);
    }

    @Override
    public OAuth2Client reset(String clientId) {
        RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(clientId);
        if (Objects.isNull(registeredClient)) {
            return null;
        }

        Tuple2<String, String> clientSecretTuple2 = PasswordUtils.clientSecret();

        RegisteredClient newRegisteredClient = RegisteredClient
                .from(registeredClient)
                .clientSecret(clientSecretTuple2.getT2())
                .build();
        this.registeredClientRepository.save(newRegisteredClient);

        return toClient(newRegisteredClient, clientSecretTuple2, false);
    }

    @Override
    public OAuth2Client get(String clientId) {
        RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(clientId);
        if (Objects.isNull(registeredClient)) {
            return null;
        }

        return toClient(registeredClient, null, true);
    }

    //==================================================================================================================

    private static OAuth2Client toClient(RegisteredClient registeredClient, Tuple2<String, String> clientSecretTuple2, boolean isDesensitized) {
        OAuth2Client clientDTO = new OAuth2Client();
        clientDTO.setClientId(registeredClient.getClientId());
        clientDTO.setClientName(registeredClient.getClientName());
        clientDTO.setRedirectUri(Optional.ofNullable(registeredClient.getRedirectUris()).flatMap(t -> t.stream().findFirst()).orElse(null));
        clientDTO.setPostLogoutRedirectUri(Optional.ofNullable(registeredClient.getPostLogoutRedirectUris()).flatMap(t -> t.stream().findFirst()).orElse(null));
        clientDTO.setScopes(registeredClient.getScopes());

        String clientSecret;
        if (Objects.nonNull(clientSecretTuple2)) {
            clientSecret = clientSecretTuple2.getT1();
        } else {
            clientSecret = registeredClient.getClientSecret();
        }
        if (isDesensitized) {
            clientDTO.setClientSecret(DesensitizedUtil.password(clientSecret));
        } else {
            clientDTO.setClientSecret(clientSecret);
        }

        return clientDTO;
    }

    private static RegisteredClient initClient(OAuth2Client dto, Tuple2<String, String> clientSecretTuple2) {
        String clientId = IdUtil.fastSimpleUUID();

        RegisteredClient.Builder builder = RegisteredClient
                .withId(IdUtil.fastSimpleUUID())
                .clientId(clientId)
                .clientSecret(clientSecretTuple2.getT2())
                // 客户端认证方式
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .scope(OidcScopes.OPENID)
                .clientName(dto.getClientName())
                .redirectUri(dto.getRedirectUri())
                .postLogoutRedirectUri(dto.getPostLogoutRedirectUri())
                .tokenSettings(TokenSettings.builder().accessTokenTimeToLive(Duration.ofDays(7)).build());

        if (CollectionUtil.isNotEmpty(dto.getScopes())) {
            builder.scopes(scopes -> scopes.addAll(dto.getScopes()));
        }

        return builder.build();
    }

}
