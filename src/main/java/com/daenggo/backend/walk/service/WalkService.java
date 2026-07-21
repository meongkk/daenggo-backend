package com.daenggo.backend.walk.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.daenggo.backend.pet.entity.Pet;
import com.daenggo.backend.pet.repository.PetRepository;
import com.daenggo.backend.user.entity.User;
import com.daenggo.backend.user.repository.UserRepository;
import com.daenggo.backend.walk.dto.WalkRequestDto.RoutePointRequest;
import com.daenggo.backend.walk.dto.WalkRequestDto.WalkCompleteRequest;
import com.daenggo.backend.walk.dto.WalkRequestDto.WalkPhotoUploadRequest;
import com.daenggo.backend.walk.dto.WalkRequestDto.WalkRouteBatchRequest;
import com.daenggo.backend.walk.dto.WalkRequestDto.WalkUpdateRequest;
import com.daenggo.backend.walk.dto.WalkResponseDto.WalkCalendarResponse;
import com.daenggo.backend.walk.dto.WalkResponseDto.WalkCompleteResponse;
import com.daenggo.backend.walk.dto.WalkResponseDto.WalkDetailResponse;
import com.daenggo.backend.walk.dto.WalkResponseDto.WalkListResponse;
import com.daenggo.backend.walk.dto.WalkResponseDto.WalkPhotoResponse;
import com.daenggo.backend.walk.dto.WalkResponseDto.WalkRouteResponse;
import com.daenggo.backend.walk.dto.WalkResponseDto.WalkStartResponse;
import com.daenggo.backend.walk.entity.WalkRecord;
import com.daenggo.backend.walk.entity.WalkRecordPet;
import com.daenggo.backend.walk.entity.walkRouteRecord;
import com.daenggo.backend.walk.repository.WalkRecordPetRepository;
import com.daenggo.backend.walk.repository.WalkRecordRepository;
import com.daenggo.backend.walk.repository.WalkRouteRecordRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WalkService {
	
	private final WalkRecordRepository walkRecordRepository;
	private final WalkRouteRecordRepository walkRouteRecordRepository;
	private final UserRepository userRepository;
	private final PetRepository petRepository;
	private final WalkRecordPetRepository walkRecordPetRepository;
	
	/**
     * 산책 시작
     */
	public WalkStartResponse startWalk(Long userId) {
		
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		WalkRecord walk = WalkRecord.builder()
				.user(user)
				.build();
		
		WalkRecord result = walkRecordRepository.save(walk);
		WalkStartResponse response = WalkStartResponse.builder()
				.walkRecordId(result.getWalkRecordId())
				.startedAt(result.getStartedAt())
				.build();
		
		
		return response;
	}

	/**
     * GPS 좌표 일괄 저장
     */
	public void saveTrackPoints(Long walkId, WalkRouteBatchRequest request) {
		WalkRecord walk = walkRecordRepository.findById(walkId)
			    .orElseThrow(() -> new IllegalArgumentException("산책이 없습니다."));
		
		List<RoutePointRequest> points = request.getTrackPoints();
		
		List<walkRouteRecord> routes = points.stream()
			    .map(point -> walkRouteRecord.builder()
			        .walkRecord(walk)
			        .sequenceNo(point.getSequenceNo())
			        .latitude(point.getLatitude())
			        .longitude(point.getLongitude())
			        .altitudeM(point.getAltitudeM())
			        .build())
			    .toList();
		
		walkRouteRecordRepository.saveAll(routes);
		
	}

	/**
     * 산책 종료
     */
	@Transactional
	public WalkCompleteResponse completeWalk(Long userId, Long walkId, WalkCompleteRequest request) {
		
		WalkRecord walk = walkRecordRepository.findById(walkId)
		        .orElseThrow(() -> new IllegalArgumentException("산책이 없습니다."));
		
		
		// 산책 시간 계산(단위 : sec)
		int durationSec = (int) Duration.between(
	            walk.getStartedAt(),
	            walk.getEndedAt()
	    ).getSeconds();
		
		// 평균 페이스 계산 (단위 : sec)
		int avgPaceSec = 0;
		if (durationSec > 0) {
	        avgPaceSec =
	        		(int) (durationSec / (request.getDistanceM().doubleValue() / 1000.0));
	    }
		
		walk.complete(request.getTitle(), 
				request.getMemo(), 
				LocalDateTime.now(),
				durationSec,
				request.getDistanceM(),
				avgPaceSec
				);
		
		WalkRecord result = walkRecordRepository.save(walk);
		
		
		// 참여 반려동물 저장
		for (Long petId : request.getPetIds()) {

	        Pet pet = petRepository.findById(petId)
	                .orElseThrow(() -> new IllegalArgumentException("반려동물이 없습니다."));

	        WalkRecordPet walkRecordPet = WalkRecordPet.builder()
	                .walkRecord(walk)
	                .pet(pet)
	                .build();

	        walkRecordPetRepository.save(walkRecordPet);
	    }
		
		
		WalkCompleteResponse response = WalkCompleteResponse.builder()
				.walkRecordId(result.getWalkRecordId())
				.endedAt(result.getEndedAt())
				.build();
		
		return response;
	}

	
	/**
     * 산책 목록 조회
     */
	public List<WalkListResponse> getWalkList(Long userId) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
     * 산책 상세 조회
     */
	public WalkDetailResponse getWalk(Long userId, Long walkId) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
     * 산책 경로 조회
     */
	public WalkRouteResponse getWalkRoute(Long userId, Long walkId) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
     * 산책 기록 수정
     */
	public WalkDetailResponse updateWalk(Long userId, Long walkId, WalkUpdateRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
     * 산책 기록 삭제
     */
	public void deleteWalk(Long userId, Long walkId) {
		// TODO Auto-generated method stub
		
	}

	/**
     * 월별 캘린더 조회
     */
	public WalkCalendarResponse getCalendar(Long userId, int year, int month) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
     * 산책 사진 등록
     */
	public WalkPhotoResponse uploadPhoto(Long userId, Long walkId, WalkPhotoUploadRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
     * 산책 사진 삭제
     */
	public void deletePhoto(Long userId, Long walkId, Long photoId) {
		// TODO Auto-generated method stub
		
	}
	
}
