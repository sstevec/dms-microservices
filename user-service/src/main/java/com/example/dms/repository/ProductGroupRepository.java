package com.example.dms.repository;

import com.example.dms.entity.ProductGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProductGroupRepository extends JpaRepository<ProductGroup, UUID> {
    List<ProductGroup> findByOwnerId(UUID ownerId);

    @Query("SELECT g FROM ProductGroup g JOIN g.products p WHERE p.id = :productAuthorizationId")
    List<ProductGroup> findGroupsByProductAuthorizationId(@Param("productAuthorizationId") UUID productAuthorizationId);

}
