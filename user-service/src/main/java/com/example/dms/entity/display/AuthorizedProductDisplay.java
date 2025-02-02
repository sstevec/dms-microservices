package com.example.dms.entity.display;
import com.example.dms.entity.ProductAuthorization;

import java.util.UUID;

public class AuthorizedProductDisplay {
    private UUID id; // UUID for ID

    private String name;

    private String description;

    private String productDetail;

    private String customerInfoTemplate;

    private String provider;

    public AuthorizedProductDisplay(ProductAuthorization authorization) {
        this.id = authorization.getId();
        this.name = authorization.getProduct().getName();
        this.description = authorization.getProduct().getDescription();
        this.productDetail = authorization.getProduct().getProductDetail();
        this.customerInfoTemplate = authorization.getProduct().getcustomerInfoTemplate();
        this.provider = authorization.getProvider().getName();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProductDetail() {
        return productDetail;
    }

    public void setProductDetail(String productDetail) {
        this.productDetail = productDetail;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getcustomerInfoTemplate() {
        return customerInfoTemplate;
    }

    public void setcustomerInfoTemplate(String customerInfoTemplate) {
        this.customerInfoTemplate = customerInfoTemplate;
    }
}
