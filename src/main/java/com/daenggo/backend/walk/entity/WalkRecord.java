package com.daenggo.backend.walk.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.daenggo.backend.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="walk_records")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalkRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="walk_record_id")
	private Long walkRecordId;
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="user_id", nullable = false)
	private User user;
	
	@Column(length=200)
	private String title;
	
	@Column(columnDefinition = "TEXT")
	private String memo;
	
	@CreationTimestamp
	@Column(name="started_at", updatable=false, nullable = false)
	private LocalDateTime startedAt;
	
	@Column(name="ended_at")
	private LocalDateTime endedAt;
	
	@Column(name="duration_sec")
	private Integer durationSec;
	
	@Column(name="distance_m", precision=12, scale=2)
	private BigDecimal distanceM;
	
	@Column(name="avg_pace_sec")
	private Integer avgPaceSec;
	
	public void complete(
			String title,
			String memo,
			LocalDateTime endedAt,
			Integer durationSec,
			BigDecimal distanceM,
			Integer avgPaceSec
			
			) {
		this.title = title;
		this.memo = memo;
		this.endedAt = endedAt;
		this.durationSec = durationSec;
		this.distanceM = distanceM;
		this.avgPaceSec = avgPaceSec;
		
	}
	
	public void update(String title, String memo) {
		this.title = title;
		this.memo = memo;
	}
	
}
