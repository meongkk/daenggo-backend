package com.daenggo.backend.walk.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.daenggo.backend.place.entity.Place;

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
@Table(name="walk_photos")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalkPhoto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="walk_photo_id")
	private Long walkPhotoId;

	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="walk_record_id", nullable = false)
	private WalkRecord walkRecord;
	
	@Column(name="image_url", length = 1000, nullable = false)
	private String imageUrl;
	
	private String caption;
	
	@Column(precision=10, scale=7)
	private BigDecimal latitude;
	
	@Column(precision=11, scale=7)
	private BigDecimal longitude;
	
	@Column(name="taken_at")
	private LocalDateTime takenAt;
	
	@CreationTimestamp
	@Column(name="created_at", updatable=false, nullable = false)
	private LocalDateTime createdAt;
	
}
