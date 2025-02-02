package com.example.dms.repository;

import com.example.dms.entity.ProductAuthorization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductAuthorizationRepository extends JpaRepository<ProductAuthorization, UUID> {
    List<ProductAuthorization> findByOwnerId(UUID ownerId);

    void deleteByProductId(UUID productId);

    List<ProductAuthorization> findByProductId(UUID productId);

    List<ProductAuthorization> findByGroupId(UUID groupId);

    List<ProductAuthorization> findByGroupIdAndOwnerId(UUID groupId, UUID childId);

    void deleteByGroupIdAndProductId(UUID groupId, UUID productId);

    ProductAuthorization findByProviderIdAndOwnerIdAndProductId(UUID providerId, UUID ownerId, UUID productId);

    ProductAuthorization findByOwnerIdAndGroupIdAndProductId(UUID providerId, UUID groupId, UUID productId);
}
