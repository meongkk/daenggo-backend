package com.daenggo.backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 스케줄러 기능 활성화
 *
 * 관광공사 장소 데이터 자동 동기화(PlaceSyncScheduler)에 사용된다.
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
}