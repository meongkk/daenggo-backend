package com.daenggo.backend.walk.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.daenggo.backend.user.entity.User;
import com.daenggo.backend.walk.dto.WalkResponseDto.WalkCalendarResponse;
import com.daenggo.backend.walk.dto.WalkResponseDto.WalkCompleteRequest;
import com.daenggo.backend.walk.dto.WalkResponseDto.WalkCompleteResponse;
import com.daenggo.backend.walk.dto.WalkResponseDto.WalkDetailResponse;
import com.daenggo.backend.walk.dto.WalkResponseDto.WalkListResponse;
import com.daenggo.backend.walk.dto.WalkResponseDto.WalkPhotoResponse;
import com.daenggo.backend.walk.dto.WalkResponseDto.WalkPhotoUploadRequest;
import com.daenggo.backend.walk.dto.WalkResponseDto.WalkRouteBatchRequest;
import com.daenggo.backend.walk.dto.WalkResponseDto.WalkRouteResponse;
import com.daenggo.backend.walk.dto.WalkResponseDto.WalkUpdateRequest;
import com.daenggo.backend.walk.dto.WalkResponseDto.WalkeStartResponse;
import com.daenggo.backend.walk.entity.WalkRecord;
import com.daenggo.backend.walk.repository.WalkRecordRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WalkServiceImpl implements WalkService {
	
	private final WalkRecordRepository walkRecordRepository;
	private final UserRepository userRepository;
	
	
	@Override
	public WalkeStartResponse startWalk(Long userId) {
		
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException());
		WalkRecord walk = WalkRecord.builder()
				.user(user)
				.build();
		
		Record result = walkRecordRepository.save(walk);
		WalkeStartResponse response = WalkStartResponse.builder()
				.walkRecordId(result.getWalkRecordId())
				.startAt(result.getStartedAt())
				.build();
		
		
		return response;
	}

	@Override
	public void saveTrackPoints(Long walkId, WalkRouteBatchRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public WalkCompleteResponse completeWalk(Long userId, Long walkId, WalkCompleteRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WalkListResponse> getWalkList(Long userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WalkDetailResponse getWalk(Long userId, Long walkId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WalkRouteResponse getWalkRoute(Long userId, Long walkId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WalkDetailResponse updateWalk(Long userId, Long walkId, WalkUpdateRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteWalk(Long userId, Long walkId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public WalkCalendarResponse getCalendar(Long userId, int year, int month) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WalkPhotoResponse uploadPhoto(Long userId, Long walkId, WalkPhotoUploadRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deletePhoto(Long userId, Long walkId, Long photoId) {
		// TODO Auto-generated method stub
		
	}
	
}
