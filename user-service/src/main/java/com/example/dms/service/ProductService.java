package com.example.dms.service;

import com.example.dms.entity.Product;
import com.example.dms.entity.ProductAuthorization;
import com.example.dms.entity.ProductGroup;
import com.example.dms.entity.User;
import com.example.dms.repository.ProductAuthorizationRepository;
import com.example.dms.repository.ProductGroupRepository;
import com.example.dms.repository.ProductRepository;
import com.example.dms.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductAuthorizationRepository authorizationRepository;
    private final UserRepository userRepository;
    private final ProductGroupRepository productGroupRepository;

    public ProductService(ProductRepository productRepository,
                          ProductAuthorizationRepository authorizationRepository,
                          UserRepository userRepository,
                          ProductGroupRepository productGroupRepository) {
        this.productRepository = productRepository;
        this.authorizationRepository = authorizationRepository;
        this.userRepository = userRepository;
        this.productGroupRepository = productGroupRepository;
    }

    @Transactional
    public Product addProduct(String name, String description, UUID ownerId, String detail) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found"));

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setOwner(owner);
        product.setProductDetail(detail);

        // upon register a new product, it should be authorized to the creator
        ProductAuthorization authorization = new ProductAuthorization();
        authorization.setProduct(product);
        authorization.setOwner(owner);
        authorization.setProvider(owner);
        productRepository.save(product);
        authorizationRepository.save(authorization);

        return product;
    }

    @Transactional
    public void removeProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        List<ProductAuthorization> authorizations = authorizationRepository.findByProductId(productId);

        for (ProductAuthorization authorization : authorizations) {
            List<ProductGroup> groups = productGroupRepository.findGroupsByProductAuthorizationId(authorization.getId());
            for (ProductGroup group : groups) {
                group.getProducts().remove(authorization);
                productGroupRepository.save(group); // Update group after removal
            }

            // Remove the authorization
            authorizationRepository.delete(authorization);
        }

        // TODO: Notify all users affected by this product removal

        productRepository.delete(product);
    }


    @Transactional
    public Product updateProduct(UUID productId, String name, String description, String detail) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (name != null) product.setName(name);
        if (description != null) product.setDescription(description);
        if (detail != null) product.setProductDetail(detail);

        // TODO: Notify all users with authorization for this product

        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByCreator(UUID ownerId) {
        return productRepository.findByOwnerId(ownerId);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByUser(UUID userId) {
        List<ProductAuthorization> authorizations = authorizationRepository.findByOwnerId(userId);
        return authorizations.stream().map(ProductAuthorization::getProduct).toList();
    }
}
