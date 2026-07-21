package com.daenggo.backend.group.repository;

import com.daenggo.backend.group.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {

    List<Group> findAllByOwnerId(Long ownerId);
}
