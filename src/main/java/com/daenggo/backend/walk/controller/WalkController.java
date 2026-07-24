package com.daenggo.backend.walk.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daenggo.backend.walk.dto.WalkRequest.WalkCompleteRequest;
import com.daenggo.backend.walk.dto.WalkRequest.WalkPhotoUploadRequest;
import com.daenggo.backend.walk.dto.WalkRequest.WalkRouteBatchRequest;
import com.daenggo.backend.walk.dto.WalkRequest.WalkUpdateRequest;
import com.daenggo.backend.walk.dto.WalkResponse.WalkCalendarResponse;
import com.daenggo.backend.walk.dto.WalkResponse.WalkCompleteResponse;
import com.daenggo.backend.walk.dto.WalkResponse.WalkDetailResponse;
import com.daenggo.backend.walk.dto.WalkResponse.WalkPhotoResponse;
import com.daenggo.backend.walk.dto.WalkResponse.WalkRouteResponse;
import com.daenggo.backend.walk.dto.WalkResponse.WalkStartResponse;
import com.daenggo.backend.walk.service.WalkService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/** JWT 로그인 사용자의 산책 기록 HTTP 요청을 처리한다. */
@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/walks")
public class WalkController {

	private final WalkService walkService;

	/** JWT 로그인 사용자의 새 산책 기록을 시작한다. */
	@PostMapping
    public ResponseEntity<WalkStartResponse> startWalk(
    		final Authentication authentication) {

		WalkStartResponse response = walkService.startWalk(authentication.getName());

	    return ResponseEntity.ok(response);
    }
	
	/** JWT 로그인 사용자가 소유한 산책에 GPS 좌표를 일괄 저장한다. */
	@PostMapping("/{walkId}/track-points/batch")
	public ResponseEntity<Void> saveRoute(
			final Authentication authentication,
			@PathVariable Long walkId, 
			@RequestBody WalkRouteBatchRequest request){

        walkService.saveTrackPoints(authentication.getName(), walkId, request);

        return ResponseEntity.ok().build();
    }
	
	/** JWT 로그인 사용자가 소유한 산책을 완료 처리한다. */
	@PatchMapping("/{walkId}/complete")
	public ResponseEntity<WalkCompleteResponse> completeWalk(
			final Authentication authentication,
	        @PathVariable Long walkId,
	        @RequestBody WalkCompleteRequest request) {

	    return ResponseEntity.ok(walkService.completeWalk(authentication.getName(), walkId, request));
	}

	/**
	 * 산책 기록 조회
	 */
	@GetMapping("/{walkId}")
	public ResponseEntity<WalkDetailResponse> getWalk(
			final Authentication authentication,
	        @PathVariable Long walkId) {

	    return ResponseEntity.ok(walkService.getWalk(authentication.getName(), walkId));
	}

	/**
	 * 산책 경로 조회
	 */
	@GetMapping("/{walkId}/route")
	public ResponseEntity<WalkRouteResponse> getRoute(
			final Authentication authentication,
	        @PathVariable Long walkId) {

	    return ResponseEntity.ok(walkService.getWalkRoute(authentication.getName(), walkId));
	}

	/**
	 * 산책 기록 수정
	 */
	@PatchMapping("/{walkId}")
	public ResponseEntity<WalkDetailResponse> updateWalk(
			final Authentication authentication,
	        @PathVariable Long walkId,
	        @RequestBody WalkUpdateRequest request) {

	    return ResponseEntity.ok(walkService.updateWalk(authentication.getName(), walkId, request));
	}

	/**
	 * 산책 기록 삭제
	 */
	@DeleteMapping("/{walkId}")
	public ResponseEntity<Void> deleteWalk(
			final Authentication authentication,
	        @PathVariable Long walkId) {

	    walkService.deleteWalk(authentication.getName(), walkId);
	    return ResponseEntity.noContent().build();
	}

	/**
	 * 월별 산책 캘린더 조회
	 */
	@GetMapping("/calendar")
	public ResponseEntity<WalkCalendarResponse> getCalendar(
			final Authentication authentication,
	        @RequestParam int year,
	        @RequestParam int month) {

	    return ResponseEntity.ok(walkService.getCalendar(authentication.getName(), year, month));
	}

	/**
	 * 산책 사진 등록
	 */
	@PostMapping(value = "/{walkId}/photos",
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<WalkPhotoResponse> uploadPhoto(
			final Authentication authentication,
	        @PathVariable Long walkId,
	        @ModelAttribute WalkPhotoUploadRequest request) {

	    return ResponseEntity.ok(walkService.uploadPhoto(authentication.getName(), walkId, request));
	}
	
	/**
	 * 산책 사진 조회
	 */
	@GetMapping("/{walkId}/photos")
	public ResponseEntity<List<WalkPhotoResponse>> getPhotos(
			final Authentication authentication,
	        @PathVariable Long walkId) {

	    return ResponseEntity.ok(
	        walkService.getPhotos(authentication.getName(), walkId)
	    );
	}


	/**
	 * 산책 사진 삭제
	 */
	@DeleteMapping("/{walkId}/photos/{photoId}")
	public ResponseEntity<Void> deletePhoto(
			final Authentication authentication,
	        @PathVariable Long walkId,
	        @PathVariable Long photoId) {

	    walkService.deletePhoto(authentication.getName(), walkId, photoId);
	    return ResponseEntity.noContent().build();
	}
	 
}
