package com.example.dms.controller;

import com.example.dms.entity.Product;
import com.example.dms.entity.ProductAuthorization;
import com.example.dms.entity.ProductGroup;
import com.example.dms.service.ProductGroupService;
import com.example.dms.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ProductGroupService productGroupService;

    public ProductController(ProductService productService, ProductGroupService productGroupService) {
        this.productService = productService;
        this.productGroupService = productGroupService;
    }

    @PostMapping("/register")
    public ResponseEntity<Product> registerProduct(@RequestParam String name, @RequestParam String description, @RequestParam String detail, @RequestParam UUID accountId) {
        Product product = productService.addProduct(name, description, accountId, detail);
        product.hideSensitiveField();
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String productId) {
        productService.removeProduct(UUID.fromString(productId));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/modify/{productId}")
    public ResponseEntity<Product> modifyProduct(@PathVariable String productId, @RequestParam(required = false) String name, @RequestParam(required = false) String description, @RequestParam(required = false) String detail) {
        Product product = productService.updateProduct(UUID.fromString(productId), name, description, detail);
        product.hideSensitiveField();
        return ResponseEntity.ok(product);
    }

    @GetMapping("/registered/{userId}")
    public ResponseEntity<List<Product>> getRegisteredProducts(@PathVariable String userId) {
        List<Product> products = productService.getProductsByCreator(UUID.fromString(userId));
        products.forEach(Product::hideSensitiveField);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/authorized/{userId}")
    public ResponseEntity<List<Product>> getAuthorizedProducts(@PathVariable String userId) {
        List<Product> products = productService.getProductsByUser(UUID.fromString(userId));
        products.forEach(Product::hideSensitiveField);
        return ResponseEntity.ok(products);
    }

    @PostMapping("/group/create")
    public ResponseEntity<String> createProductGroup(@RequestParam String name, @RequestParam UUID userId) {
        ProductGroup group = productGroupService.createProductGroup(name, userId);
        return ResponseEntity.ok(group.getId().toString());
    }

    @PostMapping("/group/add")
    public ResponseEntity<Void> addProductToGroup(@RequestParam UUID productAuthId, @RequestParam UUID userId, @RequestParam UUID groupId) {
        productGroupService.addAuthToGroup(productAuthId, userId, groupId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/group/remove")
    public ResponseEntity<Void> removeProductFromGroup(@RequestParam UUID productAuthId, @RequestParam UUID groupId) {
        productGroupService.removeAuthFromGroup(productAuthId, groupId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/group/list/{groupId}")
    public ResponseEntity<List<Product>> getAuthorizedProductsByGroup(@PathVariable String groupId) {
        List<Product> authorizations = productGroupService.getAuthsByGroup(UUID.fromString(groupId)).stream()
                .map(ProductAuthorization::getProduct).toList();
        authorizations.forEach(Product::hideSensitiveField);
        return ResponseEntity.ok(authorizations);
    }
}

