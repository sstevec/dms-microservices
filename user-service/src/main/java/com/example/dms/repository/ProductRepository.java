package com.example.dms.repository;

import com.example.dms.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByOwnerId(UUID ownerId);
}
