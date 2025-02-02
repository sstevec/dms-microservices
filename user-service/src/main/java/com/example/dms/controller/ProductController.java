package com.example.dms.controller;

import com.example.dms.entity.Product;
import com.example.dms.entity.ProductAuthorization;
import com.example.dms.entity.ProductGroup;
import com.example.dms.entity.User;
import com.example.dms.entity.display.AuthorizedProductDisplay;
import com.example.dms.entity.display.GroupDisplay;
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
    public ResponseEntity<Product> registerProduct(@RequestParam String name, @RequestParam String description, @RequestParam String detail, @RequestParam String customerInfoTemplate, @RequestParam UUID accountId) {
        Product product = productService.addProduct(name, description, accountId, detail, customerInfoTemplate);
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
    public ResponseEntity<List<AuthorizedProductDisplay>> getAuthorizedProducts(@PathVariable String userId) {
        List<ProductAuthorization> authorizations = productService.getAuthorizedProductsByUser(UUID.fromString(userId));
        List<AuthorizedProductDisplay> displays = authorizations.stream().map(ProductAuthorization::getAuthorizedProductDisplay).toList();
        return ResponseEntity.ok(displays);
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
    public ResponseEntity<List<AuthorizedProductDisplay>> getAuthorizedProductsByGroup(@PathVariable String groupId) {
        List<ProductAuthorization> authorizations = productGroupService.getAuthsByGroup(UUID.fromString(groupId));
        List<AuthorizedProductDisplay> displays = authorizations.stream().map(ProductAuthorization::getAuthorizedProductDisplay).toList();
        return ResponseEntity.ok(displays);
    }

    // Assign a user to a group
    @PostMapping("/group/{groupId}/assign")
    public ResponseEntity<Void> assignUserToGroup(
            @RequestParam UUID userId,
            @RequestParam UUID childId,
            @PathVariable UUID groupId) {
        productGroupService.assignUserToGroup(userId, childId, groupId);
        return ResponseEntity.ok().build();
    }

    // Remove a user from a group
    @DeleteMapping("/group/{groupId}/users/{childId}")
    public ResponseEntity<Void> removeUserFromGroup(
            @PathVariable UUID childId,
            @PathVariable UUID groupId) {
        productGroupService.removeUserFromGroup(childId, groupId);
        return ResponseEntity.ok().build();
    }

    // Get all users assigned to a group
    @GetMapping("/group/{groupId}/users")
    public ResponseEntity<List<User>> getUsersByGroup(@PathVariable UUID groupId) {
        List<User> users = productGroupService.getUsersByGroup(groupId);
        users.forEach(User::hideSensitiveField);
        return ResponseEntity.ok(users);
    }

    // Delete a group
    @DeleteMapping("/group/delete/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable UUID groupId) {
        productGroupService.deleteGroup(groupId);
        return ResponseEntity.ok().build();
    }

    // Get all groups owned by a user
    @GetMapping("/group/owner/{userId}")
    public ResponseEntity<List<GroupDisplay>> getGroupsByUser(@PathVariable UUID userId) {
        List<ProductGroup> groups = productGroupService.getGroupsByUser(userId);
        List<GroupDisplay> displays = groups.stream().map(ProductGroup::getDisplayableInfo).toList();
        return ResponseEntity.ok(displays);
    }

    // Get all groups assigned to a user
    @GetMapping("/group/user/{userId}")
    public ResponseEntity<List<GroupDisplay>> getGroupsAssignedToUser(@PathVariable UUID userId) {
        List<ProductGroup> groups = productGroupService.getGroupsAssignedToUser(userId);
        List<GroupDisplay> displays = groups.stream().map(ProductGroup::getDisplayableInfo).toList();
        return ResponseEntity.ok(displays);
    }
}

