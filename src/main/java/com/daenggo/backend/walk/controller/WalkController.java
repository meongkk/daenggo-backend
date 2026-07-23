package com.daenggo.backend.walk.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/walks")
public class WalkController {

	private final WalkService walkService;

	@PostMapping
    public ResponseEntity<WalkStartResponse> startWalk(
    		@RequestParam Long userId) {

		WalkStartResponse response = walkService.startWalk(userId);

	    return ResponseEntity.ok(response);
    }
	
	@PostMapping("/{walkId}/track-points/batch")
	public ResponseEntity<Void> saveRoute(
			@RequestParam Long userId, 
			@PathVariable Long walkId, 
			@RequestBody WalkRouteBatchRequest request){

        walkService.saveTrackPoints(walkId, request);

        return ResponseEntity.ok().build();
    }
	
	@PatchMapping("/{walkId}/complete")
	public ResponseEntity<WalkCompleteResponse> completeWalk(
	        @RequestParam Long userId,
	        @PathVariable Long walkId,
	        @RequestBody WalkCompleteRequest request) {

	    return ResponseEntity.ok(walkService.completeWalk(userId, walkId, request));
	}

	/**
	 * 산책 기록 조회
	 */
	@GetMapping("/{walkId}")
	public ResponseEntity<WalkDetailResponse> getWalk(
	        @RequestParam Long userId,
	        @PathVariable Long walkId) {

	    return ResponseEntity.ok(walkService.getWalk(userId, walkId));
	}

	/**
	 * 산책 경로 조회
	 */
	@GetMapping("/{walkId}/route")
	public ResponseEntity<WalkRouteResponse> getRoute(
	        @RequestParam Long userId,
	        @PathVariable Long walkId) {

	    return ResponseEntity.ok(walkService.getWalkRoute(userId, walkId));
	}

	/**
	 * 산책 기록 수정
	 */
	@PatchMapping("/{walkId}")
	public ResponseEntity<WalkDetailResponse> updateWalk(
	        @RequestParam Long userId,
	        @PathVariable Long walkId,
	        @RequestBody WalkUpdateRequest request) {

	    return ResponseEntity.ok(walkService.updateWalk(userId, walkId, request));
	}

	/**
	 * 산책 기록 삭제
	 */
	@DeleteMapping("/{walkId}")
	public ResponseEntity<Void> deleteWalk(
	        @RequestParam Long userId,
	        @PathVariable Long walkId) {

	    walkService.deleteWalk(userId, walkId);
	    return ResponseEntity.noContent().build();
	}

	/**
	 * 월별 산책 캘린더 조회
	 */
	@GetMapping("/calendar")
	public ResponseEntity<WalkCalendarResponse> getCalendar(
	        @RequestParam Long userId,
	        @RequestParam int year,
	        @RequestParam int month) {

	    return ResponseEntity.ok(walkService.getCalendar(userId, year, month));
	}

	/**
	 * 산책 사진 등록
	 */
	@PostMapping(value = "/{walkId}/photos",
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<WalkPhotoResponse> uploadPhoto(
	        @RequestParam Long userId,
	        @PathVariable Long walkId,
	        @ModelAttribute WalkPhotoUploadRequest request) {

	    return ResponseEntity.ok(walkService.uploadPhoto(userId, walkId, request));
	}

	/**
	 * 산책 사진 삭제
	 */
	@DeleteMapping("/{walkId}/photos/{photoId}")
	public ResponseEntity<Void> deletePhoto(
	        @RequestParam Long userId,
	        @PathVariable Long walkId,
	        @PathVariable Long photoId) {

	    walkService.deletePhoto(userId, walkId, photoId);
	    return ResponseEntity.noContent().build();
	}
	 
}
