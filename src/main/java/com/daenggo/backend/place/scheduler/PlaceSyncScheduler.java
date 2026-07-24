package com.daenggo.backend.place.scheduler;

import com.daenggo.backend.place.api.AreaCode;
import com.daenggo.backend.place.service.PlaceSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 관광공사 장소 데이터 자동 동기화
 *
 * 관광공사 데이터는 일 1회 갱신되므로 매일 새벽에 전국을 순회한다.
 * 기존 장소는 수정일시가 변경된 경우에만 상세를 재조회하므로,
 * 실제 API 호출량은 변경된 장소 수에 비례한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceSyncScheduler {

    private final PlaceSyncService placeSyncService;

    /** 매일 새벽 4시에 전국 장소를 동기화한다 */
    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void dailySync() {

        log.info("장소 자동 동기화 시작");
        int total = 0;

        for (AreaCode area : AreaCode.ALL) {
            try {
                total += placeSyncService.syncByArea(area.code(), area.name());
            } catch (Exception e) {
                log.error("지역 동기화 실패: {}", area.name(), e);
            }
        }

        log.info("장소 자동 동기화 완료: 신규 {}건", total);
    }
}