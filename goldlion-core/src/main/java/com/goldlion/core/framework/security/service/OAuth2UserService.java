package com.goldlion.core.framework.security.service;

import com.goldlion.core.framework.security.domain.model.OAuth2User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * <p>
 * OAuth2用户 服务类
 * </p>
 * 项目如果使用OAuth2，需要实现类否则不需要
 *
 * @see com.goldlion.scrm.aggregation.overt.service.impl.OAuth2UserServiceImpl
 */
public interface OAuth2UserService extends UserDetailsService {

    OAuth2User loadUser(String username);

    @Override
    default UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.loadUser(username);
    }

}
