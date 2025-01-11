package com.example.dms.repository;

import com.example.dms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findByParentAccountId(UUID parentId);

    Optional<User> findByIdAndParentAccountId(UUID id, UUID parentId);

    List<User> findByIdIn(List<UUID> ids);

    Optional<User> findByEmail(String email);
}
