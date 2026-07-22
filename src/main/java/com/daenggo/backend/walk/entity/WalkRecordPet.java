package com.daenggo.backend.walk.entity;

import com.daenggo.backend.pet.entity.Pet;

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
@Table(name = "walk_record_pets")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalkRecordPet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="walk_record_pet_id")
	private Long walkRecordPetId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="pet_id", nullable = false)
	private Pet pet;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="walk_record_id", nullable = false)
	private WalkRecord walkRecord;
}
