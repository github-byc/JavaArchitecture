package com.goldlion.core.framework.security.domain.utils;

import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.goldlion.core.common.function.Tuple2;
import org.springframework.security.crypto.password.PasswordEncoder;

public abstract class PasswordUtils {

    private PasswordUtils() {
    }

    public static String encode(String password) {
        PasswordEncoder passwordEncoder = SpringUtil.getBean(PasswordEncoder.class);
        return passwordEncoder.encode(password);
    }

    public static Tuple2<String, String> clientSecret() {
        String clientSecret = IdUtil.fastSimpleUUID();
        return new Tuple2<>(clientSecret, encode(clientSecret));
    }

}
