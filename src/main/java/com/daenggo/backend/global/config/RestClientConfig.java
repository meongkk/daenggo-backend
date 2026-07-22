package com.daenggo.backend.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/** 외부 API 호출 관련 빈 설정 */
@Configuration
public class RestClientConfig {

    /** 외부 API 호출용 RestClient */
    @Bean
    public RestClient restClient() {
        return RestClient.builder().build();
    }

    /** JSON 파싱용 ObjectMapper */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}