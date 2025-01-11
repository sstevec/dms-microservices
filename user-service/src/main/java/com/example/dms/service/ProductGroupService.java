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

        group.getProducts().add(productAuthorization);
        productGroupRepository.save(group);
    }

    @Transactional
    public void removeAuthFromGroup(UUID productAuthId, UUID groupId) {
        ProductGroup group = productGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        ProductAuthorization productAuthorization = productAuthorizationRepository.findById(productAuthId)
                .orElseThrow(() -> new IllegalArgumentException("Product authorization not found"));

        group.getProducts().remove(productAuthorization);
        productGroupRepository.save(group);
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

        productAuthorizationRepository.deleteAll(productAuthorizations);

        // TODO notify the user they are being removed from the group
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
}
