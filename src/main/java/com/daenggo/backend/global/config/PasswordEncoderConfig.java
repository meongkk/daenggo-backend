package com.daenggo.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 비밀번호 암호화 설정
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * 비밀번호 암호화 객체 생성
     *
     * @return 비밀번호 암호화 객체
     */
    @Bean
    public PasswordEncoder passwordEncoder() {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("1"));

        return new BCryptPasswordEncoder();
    }
}
