package com.example.dms.service;

import com.example.dms.entity.*;
import com.example.dms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProductGroupService {

    private final ProductGroupRepository productGroupRepository;
    private final ProductAuthorizationRepository productAuthorizationRepository;
    private final UserGroupRelationshipRepository userGroupRelationshipRepository;
    private final UserRelationshipRepository userRelationshipRepository;
    private final UserRepository userRepository;

    public ProductGroupService(
            ProductGroupRepository productGroupRepository,
            ProductAuthorizationRepository productAuthorizationRepository,
            UserGroupRelationshipRepository userGroupRelationshipRepository,
            UserRelationshipRepository userRelationshipRepository,
            UserRepository userRepository) {
        this.productGroupRepository = productGroupRepository;
        this.productAuthorizationRepository = productAuthorizationRepository;
        this.userGroupRelationshipRepository = userGroupRelationshipRepository;
        this.userRelationshipRepository = userRelationshipRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProductGroup createProductGroup(String name, UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        ProductGroup group = new ProductGroup();
        group.setName(name);
        group.setOwner(user);
        return productGroupRepository.save(group);
    }

    @Transactional
    public void addAuthToGroup(UUID productAuthId, UUID userId, UUID groupId) {
        ProductGroup group = productGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        if (!group.getOwner().getId().equals(userId)) {
            throw new IllegalStateException("User does not own the group");
        }

        ProductAuthorization productAuthorization = productAuthorizationRepository.findById(productAuthId)
                .orElseThrow(() -> new IllegalArgumentException("Product authorization not found"));

        if (group.getProducts().contains(productAuthorization)) {
            // already in the group
            return;
        }
        group.getProducts().add(productAuthorization);
        productGroupRepository.save(group);

        // All users assigned to the group will have the auth of the product
        List<User> assignedUsers = userGroupRelationshipRepository.findByGroupId(groupId)
                .stream().map(UserGroupRelationship::getUser).toList();
        for (User user : assignedUsers) {
            ProductAuthorization auth = new ProductAuthorization();
            auth.setProduct(productAuthorization.getProduct());
            auth.setProductGroup(group);
            auth.setOwner(user);
            auth.setProvider(group.getOwner());
            productAuthorizationRepository.save(auth);
        }
    }

    @Transactional
    public void removeAuthFromGroup(UUID productAuthId, UUID groupId) {
        ProductGroup group = productGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        ProductAuthorization productAuthorization = productAuthorizationRepository.findById(productAuthId)
                .orElseThrow(() -> new IllegalArgumentException("Product authorization not found"));

       removeAuthFromGroup(productAuthorization, group);
    }

    @Transactional
    public void removeAuthFromGroup(ProductAuthorization authorization, ProductGroup group) {
        group.getProducts().remove(authorization);
        productGroupRepository.save(group);

        // all users in the group will lose their auth from the given auth
        List<User> assignedUsers = userGroupRelationshipRepository.findByGroupId(group.getId())
                .stream().map(UserGroupRelationship::getUser).toList();
        for (User user : assignedUsers) {
            ProductAuthorization auth = productAuthorizationRepository.findByOwnerIdAndGroupIdAndProductId(user.getId(), group.getId(), authorization.getProduct().getId());
            removeProductAuthorization(auth);
        }
    }


    @Transactional(readOnly = true)
    public List<ProductAuthorization> getAuthsByGroup(UUID groupId) {
        ProductGroup group = productGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        return List.copyOf(group.getProducts());
    }

    @Transactional
    public void assignUserToGroup(UUID userId, UUID childId, UUID groupId) {
        User child = userRepository.findById(childId).orElseThrow(() -> new IllegalArgumentException("Child not found"));

        UserRelationship relationship = userRelationshipRepository
                .findByParentIdAndChildId(userId, childId)
                .orElseThrow(() -> new IllegalArgumentException("No valid relationship between user and child"));

        ProductGroup group = productGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        if (!group.getOwner().getId().equals(userId)) {
            throw new IllegalStateException("User does not own the group");
        }

        UserGroupRelationship userGroupRelationship = new UserGroupRelationship();
        userGroupRelationship.setUser(child);
        userGroupRelationship.setGroup(group);

        userGroupRelationshipRepository.save(userGroupRelationship);

        // upon user get into the group, he should obtain all auth provided by the group
        List<ProductAuthorization> authorizations = getAuthsByGroup(groupId);
        for (ProductAuthorization authorization : authorizations) {
            ProductAuthorization subAuth = new ProductAuthorization();
            subAuth.setProduct(authorization.getProduct());
            subAuth.setProductGroup(group);
            subAuth.setOwner(child);
            subAuth.setProvider(group.getOwner());
            productAuthorizationRepository.save(subAuth);
        }
    }

    @Transactional
    public void removeUserFromGroup(UUID childId, UUID groupId) {
        List<UserGroupRelationship> relationships = userGroupRelationshipRepository
                .findByUserIdAndGroupId(childId, groupId);

        if (relationships.isEmpty()) {
            throw new IllegalArgumentException("User is not assigned to the group");
        }

        // Remove all relationships between the user and the group
        userGroupRelationshipRepository.deleteAll(relationships);

        List<ProductAuthorization> productAuthorizations = productAuthorizationRepository
                .findByGroupIdAndOwnerId(groupId, childId);
        // for all auth obtained from the given group, the user should remove them
        for (ProductAuthorization productAuthorization : productAuthorizations) {
            removeProductAuthorization(productAuthorization);
        }
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByGroup(UUID groupId) {
        List<UserGroupRelationship> relationships = userGroupRelationshipRepository
                .findByGroupId(groupId);
        return relationships.stream().map(UserGroupRelationship::getUser).toList();
    }

    @Transactional
    public void deleteGroup(UUID groupId) {
        List<UserGroupRelationship> relationships = userGroupRelationshipRepository.findByGroupId(groupId);

        for (UserGroupRelationship relationship : relationships) {
            removeUserFromGroup(relationship.getUser().getId(), groupId);
        }

        productGroupRepository.deleteById(groupId);
    }

    @Transactional(readOnly = true)
    public List<ProductGroup> getGroupsByUser(UUID userId) {
        return productGroupRepository.findByOwnerId(userId);
    }

    @Transactional(readOnly = true)
    public List<ProductGroup> getGroupsAssignedToUser(UUID userId) {
        List<UserGroupRelationship> relationships = userGroupRelationshipRepository
                .findByUserId(userId);
        return relationships.stream().map(UserGroupRelationship::getGroup).toList();
    }

    @Transactional
    public void removeProductAuthorization(ProductAuthorization productAuthorization) {
        // find all the groups that use this authorization
        List<ProductGroup> groups = productGroupRepository.findGroupsByProductAuthorizationId(productAuthorization.getId());
        // for each group, remove this auth
        for (ProductGroup group : groups) {
            removeAuthFromGroup(productAuthorization, group);
        }

        productAuthorizationRepository.deleteById(productAuthorization.getId());
    }
}
