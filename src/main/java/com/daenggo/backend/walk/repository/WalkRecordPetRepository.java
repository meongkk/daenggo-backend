package com.daenggo.backend.walk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daenggo.backend.walk.entity.WalkRecord;
import com.daenggo.backend.walk.entity.WalkRecordPet;

public interface WalkRecordPetRepository extends JpaRepository<WalkRecordPet, Long> {

    List<WalkRecordPet> findByWalkRecord(WalkRecord walkRecord);

    void deleteByWalkRecord(WalkRecord walkRecord);

}
