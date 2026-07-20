package com.daenggo.backend.walk.entity;

import java.math.BigDecimal;

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
@Table(name="walk_route_records")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class walkRouteRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="walk_route_record_id")
	private Long walkRouteRecordId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="walk_record_id", nullable = false)
	private WalkRecord walkRecord;
	
	@Column(name="sequence_no", nullable = false)
	private Long sequenceNo;
	
	@Column(precision=10, scale=7, nullable = false)
	private BigDecimal latitude;
	
	@Column(precision=11, scale=7, nullable = false)
	private BigDecimal longitude;
	
	@Column(name="altitude_m", precision=8, scale=2, nullable = false)
	private BigDecimal altitudeM;
	
	
}
