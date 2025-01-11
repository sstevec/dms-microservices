package com.example.dms.repository;

import com.example.dms.entity.UserRelationship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRelationshipRepository extends JpaRepository<UserRelationship, UUID> {

    Optional<UserRelationship> findByParentIdAndChildId(UUID parentId, UUID childId);

    List<UserRelationship> findByParentId(UUID parentId);

    List<UserRelationship> findByChildId(UUID childId);
}
