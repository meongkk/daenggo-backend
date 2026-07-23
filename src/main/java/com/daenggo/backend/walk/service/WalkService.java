package com.daenggo.backend.walk.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.daenggo.backend.pet.entity.Pet;
import com.daenggo.backend.pet.repository.PetRepository;
import com.daenggo.backend.user.entity.User;
import com.daenggo.backend.user.repository.UserRepository;
import com.daenggo.backend.walk.dto.WalkRequest.RoutePointRequest;
import com.daenggo.backend.walk.dto.WalkRequest.WalkCompleteRequest;
import com.daenggo.backend.walk.dto.WalkRequest.WalkPhotoUploadRequest;
import com.daenggo.backend.walk.dto.WalkRequest.WalkRouteBatchRequest;
import com.daenggo.backend.walk.dto.WalkRequest.WalkUpdateRequest;
import com.daenggo.backend.walk.dto.WalkResponse.RoutePointResponse;
import com.daenggo.backend.walk.dto.WalkResponse.WalkCalendarItem;
import com.daenggo.backend.walk.dto.WalkResponse.WalkCalendarResponse;
import com.daenggo.backend.walk.dto.WalkResponse.WalkCompleteResponse;
import com.daenggo.backend.walk.dto.WalkResponse.WalkDetailResponse;
import com.daenggo.backend.walk.dto.WalkResponse.WalkPhotoResponse;
import com.daenggo.backend.walk.dto.WalkResponse.WalkRouteResponse;
import com.daenggo.backend.walk.dto.WalkResponse.WalkStartResponse;
import com.daenggo.backend.walk.entity.WalkPhoto;
import com.daenggo.backend.walk.entity.WalkRecord;
import com.daenggo.backend.walk.entity.WalkRecordPet;
import com.daenggo.backend.walk.entity.WalkRouteRecord;
import com.daenggo.backend.walk.repository.WalkPhotoRepository;
import com.daenggo.backend.walk.repository.WalkRecordPetRepository;
import com.daenggo.backend.walk.repository.WalkRecordRepository;
import com.daenggo.backend.walk.repository.WalkRouteRecordRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WalkService {
	
	private final WalkRecordRepository walkRecordRepository;
	private final WalkRouteRecordRepository walkRouteRecordRepository;
	private final UserRepository userRepository;
	private final PetRepository petRepository;
	private final WalkRecordPetRepository walkRecordPetRepository;
	private final WalkPhotoRepository walkPhotoRepository;
	
	/**
     * 산책 시작
     */
	public WalkStartResponse startWalk(String email) {
		
		final User user = userRepository.findByEmailAndDeletedAtIsNull(email)
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
		
		List<WalkRouteRecord> routes = points.stream()
			    .map(point -> WalkRouteRecord.builder()
			        .walkRecord(walk)
			        .sequenceNo(point.getSequenceNo())
			        .latitude(point.getLatitude())
			        .longitude(point.getLongitude())
			        .build())
			    .toList();
		
		walkRouteRecordRepository.saveAll(routes);
		
	}

	/**
     * 산책 종료
     */
	@Transactional
	public WalkCompleteResponse completeWalk(Long walkId, WalkCompleteRequest request) {
		
		WalkRecord walk = walkRecordRepository.findById(walkId)
		        .orElseThrow(() -> new IllegalArgumentException("산책이 없습니다."));
		
		
		
		// 산책 시간 계산(단위 : sec)
		LocalDateTime endedAt = LocalDateTime.now();
		
		
		int durationSec = (int) Duration.between(
	            walk.getStartedAt(),
	            endedAt
	    ).getSeconds();
		
		// 평균 페이스 계산 (단위 : sec)
		int avgPaceSec = 0;
		if (request.getDistanceM() != null 
				&& request.getDistanceM().doubleValue() > 0) {
			
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
     * 산책 상세 조회
     */
	public WalkDetailResponse getWalk(String email, Long walkId) {
		
		final User user = userRepository.findByEmailAndDeletedAtIsNull(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		

		WalkRecord walk = walkRecordRepository.findByWalkRecordIdAndUserAndIsDeletedFalse(walkId, user)
				.orElseThrow(() -> new IllegalArgumentException("산책 기록을 찾을 수 없습니다."));

		
		List<Long> petIds = walkRecordPetRepository.findByWalkRecord(walk)
				.stream()
				.map(pet -> pet.getPet().getId())
				.toList();
		
		WalkDetailResponse response = WalkDetailResponse.builder()
				.walkRecordId(walk.getWalkRecordId())
				.title(walk.getTitle())
				.memo(walk.getMemo())
				.startedAt(walk.getStartedAt())
				.distanceM(walk.getDistanceM())
				.durationSec(walk.getDurationSec())
				.avgPaceSec(walk.getAvgPaceSec())
				.petIds(petIds)
				.build();
		
		return response;
	}

	/**
     * 산책 경로 조회
     */
	public WalkRouteResponse getWalkRoute(String email, Long walkId) {
		final User user = userRepository.findByEmailAndDeletedAtIsNull(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		

		WalkRecord walk = walkRecordRepository.findByWalkRecordIdAndUserAndIsDeletedFalse(walkId, user)
				.orElseThrow(() -> new IllegalArgumentException("산책 기록을 찾을 수 없습니다."));
		
		List<WalkRouteRecord> routeRecords = walkRouteRecordRepository.findByWalkRecordOrderBySequenceNoAsc(walk);
		
		List<RoutePointResponse> routePoints = routeRecords.stream()
				.map(route -> RoutePointResponse.builder()
						.sequenceNo(route.getSequenceNo())
						.latitude(route.getLatitude())
						.longitude(route.getLongitude())
						.build()
						).toList();
		
		WalkRouteResponse response = WalkRouteResponse.builder()
				.routePoints(routePoints)
				.build();
		
		return response;
	}

	/**
     * 산책 기록 수정
     */
	@Transactional
	public WalkDetailResponse updateWalk(String email, Long walkId, WalkUpdateRequest request) {
		
		final User user = userRepository.findByEmailAndDeletedAtIsNull(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		

		WalkRecord walk = walkRecordRepository.findByWalkRecordIdAndUserAndIsDeletedFalse(walkId, user)
				.orElseThrow(() -> new IllegalArgumentException("산책 기록을 찾을 수 없습니다."));
		
		walk.update(request.getTitle(), request.getMemo());
		
		List<Long> petIds = walkRecordPetRepository.findByWalkRecord(walk)
				.stream()
				.map(pet -> pet.getPet().getId())
				.toList();
		
		WalkDetailResponse response = WalkDetailResponse.builder()
				.walkRecordId(walk.getWalkRecordId())
				.title(walk.getTitle())
				.memo(walk.getMemo())
				.startedAt(walk.getStartedAt())
				.distanceM(walk.getDistanceM())
				.durationSec(walk.getDurationSec())
				.avgPaceSec(walk.getAvgPaceSec())
				.petIds(petIds)
				.build();
		
		return response;
	}

	/**
     * 산책 기록 삭제
     */
	@Transactional
	public void deleteWalk(String email, Long walkId) {
		final User user = userRepository.findByEmailAndDeletedAtIsNull(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		
		WalkRecord walk = walkRecordRepository.findByWalkRecordIdAndUserAndIsDeletedFalse(walkId, user)
				.orElseThrow(() -> new IllegalArgumentException("산책 기록을 찾을 수 없습니다."));
		
		walk.delete();
		
	}

	/**
     * 월별 산책 목록 조회(캘린더용)
     */
	public WalkCalendarResponse getCalendar(String email, int year, int month) {
		final User user = userRepository.findByEmailAndDeletedAtIsNull(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		
		LocalDate firstDay = LocalDate.of(year, month, 1);

		LocalDateTime start = firstDay.atStartOfDay();
		LocalDateTime end = firstDay.plusMonths(1).atStartOfDay();
		
		List<WalkRecord> walks = walkRecordRepository.findByUserAndStartedAtBetweenAndIsDeletedFalse(user, start, end);
		
	    List<WalkCalendarItem> items = walks.stream()
	    		.map(i -> WalkCalendarItem.builder()
	    				.walkRecordId(i.getWalkRecordId())
	    				.walkDate(i.getStartedAt().toLocalDate())
	    				.build())
	    		.toList();
	    
	    WalkCalendarResponse response = WalkCalendarResponse.builder()
	    		.walks(items)
	    		.build();
	    
		return response;
	}

	/**
     * 산책 사진 등록
     */
	public WalkPhotoResponse uploadPhoto(String email, Long walkId, WalkPhotoUploadRequest request) {
		final User user = userRepository.findByEmailAndDeletedAtIsNull(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		
		WalkRecord walk = walkRecordRepository.findByWalkRecordIdAndUserAndIsDeletedFalse(walkId, user)
				.orElseThrow(() -> new IllegalArgumentException("산책 기록을 찾을 수 없습니다."));
		
		MultipartFile image = request.getImage();

	    if (image == null || image.isEmpty()) {
	        throw new IllegalArgumentException("이미지가 없습니다.");
	    }


	    // 파일 저장
	    String originalFilename =
	            image.getOriginalFilename() != null 
	            ? image.getOriginalFilename()
	            : "image";

	    String fileName = UUID.randomUUID() 
	            + "_" 
	            + originalFilename;


	    String uploadPath = "uploads/walk/";

	    Path filePath = Paths.get(uploadPath + fileName);


	    try {
	        Files.createDirectories(filePath.getParent());
	        image.transferTo(filePath);

	    } catch (IOException e) {
	        throw new RuntimeException("파일 저장 실패", e);
	    }


	    // DB 저장용 URL 생성
	    String imageUrl = "/uploads/walk/" + fileName;
	   
	    // 3. Entity 생성
	    WalkPhoto photo = WalkPhoto.builder()
	            .walkRecord(walk)
	            .imageUrl(imageUrl)
	            .caption(request.getCaption())
	            .latitude(request.getLatitude())
	            .longitude(request.getLongitude())
	            .takenAt(request.getTakenAt())
	            .build();

	    WalkPhoto savedPhoto = walkPhotoRepository.save(photo);

	    WalkPhotoResponse response = WalkPhotoResponse.builder()
        .walkPhotoId(savedPhoto.getWalkPhotoId())
        .imageUrl(savedPhoto.getImageUrl())
        .caption(savedPhoto.getCaption())
        .latitude(savedPhoto.getLatitude())
        .longitude(savedPhoto.getLongitude())
        .takenAt(savedPhoto.getTakenAt())
        .build();

	    return response;
	}
	
	/**
     * 산책 사진 조회
     */
	@Transactional(readOnly = true)
	public List<WalkPhotoResponse> getPhotos(String email, Long walkId) {

	    WalkRecord walkRecord = walkRecordRepository.findById(walkId)
	            .orElseThrow(() -> new IllegalArgumentException("산책 기록이 없습니다."));

	    // 본인 산책인지 확인
	    if (!walkRecord.getUser().getEmail().equals(email)) {
	        throw new IllegalArgumentException("접근 권한이 없습니다.");
	    }


	    List<WalkPhoto> photos =
	            walkPhotoRepository.findByWalkRecord_WalkRecordId(walkId);


	    return photos.stream()
	            .map(photo -> WalkPhotoResponse.builder()
	                    .walkPhotoId(photo.getWalkPhotoId())
	                    .imageUrl(photo.getImageUrl())
	                    .caption(photo.getCaption())
	                    .takenAt(photo.getTakenAt())
	                    .latitude(photo.getLatitude())
	                    .longitude(photo.getLongitude())
	                    .build()
	            )
	            .toList();
	}

	/**
     * 산책 사진 삭제
     */
	@Transactional
	public void deletePhoto(String email, Long walkId, Long photoId) {
		
		WalkPhoto photo = walkPhotoRepository.findById(photoId)
				.orElseThrow(() -> new IllegalArgumentException("사진을 찾을 수 없습니다."));
		
	    // 해당 사진이 요청한 산책의 사진인지 확인
	    if (!photo.getWalkRecord().getWalkRecordId().equals(walkId)) {
	        throw new IllegalArgumentException("해당 산책의 사진이 아닙니다.");
	    }

	    // 해당 산책의 주인이 맞는지 확인
	    if (!photo.getWalkRecord().getUser().getEmail().equals(email)) {
	        throw new IllegalArgumentException("삭제 권한이 없습니다.");
	    }

	    deleteImageFile(photo.getImageUrl());
	    walkPhotoRepository.delete(photo);
	}
	
	/**
	 * 로컬 저장 이미지 삭제
	 */
	private void deleteImageFile(String imageUrl) {

	    try {
	        Path filePath = Paths.get("." + imageUrl);

	        Files.deleteIfExists(filePath);

	    } catch (IOException e) {
	        throw new RuntimeException("이미지 파일 삭제 실패", e);
	    }
	}
	
	
}
