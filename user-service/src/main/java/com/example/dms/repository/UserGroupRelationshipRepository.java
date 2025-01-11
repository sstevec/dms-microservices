package com.example.dms.repository;

import com.example.dms.entity.UserGroupRelationship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserGroupRelationshipRepository extends JpaRepository<UserGroupRelationship, UUID> {
    List<UserGroupRelationship> findByGroupId(UUID groupId);

    List<UserGroupRelationship> findByUserId(UUID userId);

    List<UserGroupRelationship> findByUserIdAndGroupId(UUID userId, UUID groupId);
}
