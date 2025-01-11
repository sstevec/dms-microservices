package com.example.dms.service;

import com.example.dms.entity.User;
import com.example.dms.entity.UserRelationship;
import com.example.dms.entity.display.UserInfoDisplay;
import com.example.dms.repository.UserRelationshipRepository;
import com.example.dms.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserRelationshipService {

    private final UserRepository userRepository;
    private final UserRelationshipRepository relationshipRepository;

    public UserRelationshipService(UserRepository userRepository, UserRelationshipRepository relationshipRepository) {
        this.userRepository = userRepository;
        this.relationshipRepository = relationshipRepository;
    }

    @Transactional
    public UserRelationship addRelationship(User parent, User child) {
        if (parent.getAccountType() != User.AccountType.PAID) {
            throw new IllegalStateException("Parent must be a paid user to add relationships");
        }

        if (child.getAccountType() == User.AccountType.FREE && !parent.equals(child.getParentAccount())) {
            throw new IllegalStateException("Parent must be the creator of the free child user");
        }

        UserRelationship relationship = new UserRelationship();
        relationship.setParent(parent);
        relationship.setChild(child);

        return relationshipRepository.save(relationship);
    }


    @Transactional
    public UserRelationship addRelationship(UUID parentId, UUID childId) {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent user not found"));

        User child = userRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("Child user not found"));

        return addRelationship(parent, child);
    }

    @Transactional
    public void deleteRelationship(UUID parentId, UUID childId) {
        UserRelationship relationship = relationshipRepository.findByParentIdAndChildId(parentId, childId)
                .orElseThrow(() -> new IllegalArgumentException("Relationship not found"));

        User parent = relationship.getParent();
        User child = relationship.getChild();

        if (child.getAccountType() == User.AccountType.FREE && child.getParentAccount().equals(parent)) {
            throw new IllegalStateException("Cannot remove relationships for free users created by the parent");
        }

        // TODO: before delete the relationship, all the authorized product needs to be removed, as well as groups

        relationshipRepository.delete(relationship);
    }

    @Transactional(readOnly = true)
    public List<UserRelationship> getChildrenByParent(UUID parentId) {
        return relationshipRepository.findByParentId(parentId);
    }

    @Transactional(readOnly = true)
    public List<UserRelationship> getParentsByChild(UUID childId) {
        return relationshipRepository.findByChildId(childId);
    }

    @Transactional(readOnly = true)
    public List<UserInfoDisplay> getLinks(UUID userId, boolean includeF, boolean includeD, boolean includeP) {
        List<UserInfoDisplay> result = new ArrayList<>();
        if (includeF || includeD) {
            // obtain all the children
            List<UserRelationship> children = relationshipRepository.findByParentId(userId);
            List<UserInfoDisplay> infos = children.stream().map(UserRelationship::extractChildInfo).toList();

            if (includeF && !includeD) {
                infos = infos.stream().filter(obj -> "Free account".equals(obj.getRelation())).toList();
            } else if (!includeF) {
                infos = infos.stream().filter(obj -> "Distributor".equals(obj.getRelation())).toList();
            }
            result.addAll(infos);

        }

        if (includeP){
            List<UserRelationship> parents = relationshipRepository.findByChildId(userId);
            List<UserInfoDisplay> infos = parents.stream().map(UserRelationship::extractParentInfo).toList();
            result.addAll(infos);
        }
        return result;
    }

    public boolean checkRelationship(UUID parentId, UUID childId) {
        Optional<UserRelationship> relationship = relationshipRepository.findByParentIdAndChildId(parentId, childId);
        return relationship.isPresent();
    }
}
