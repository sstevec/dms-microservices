package com.example.dms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_authorizations")
public class ProductAuthorization {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id; // UUID for ID

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner; // The user authorized to use the product

    @ManyToOne
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider; // The user who authorized the product

    @ManyToOne
    @JoinColumn(name = "group_id")
    private ProductGroup group; // The user who authorized the product

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public User getProvider() {
        return provider;
    }

    public void setProvider(User provider) {
        this.provider = provider;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ProductGroup getProductGroup() {
        return group;
    }

    public void setProductGroup(ProductGroup productGroup) {
        this.group = productGroup;
    }
}
