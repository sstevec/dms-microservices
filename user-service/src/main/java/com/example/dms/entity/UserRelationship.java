package com.example.dms.entity;

import com.example.dms.entity.display.UserInfoDisplay;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_relationships")
public class UserRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id; // UUID for ID

    @ManyToOne
    @JoinColumn(name = "parent_id", nullable = false)
    private User parent;

    @ManyToOne
    @JoinColumn(name = "child_id", nullable = false)
    private User child;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getParent() {
        return parent;
    }

    public void setParent(User parent) {
        this.parent = parent;
    }

    public User getChild() {
        return child;
    }

    public void setChild(User child) {
        this.child = child;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public UserInfoDisplay extractChildInfo(){
        UserInfoDisplay info = new UserInfoDisplay(child);
        if (child.getAccountType() == User.AccountType.FREE){
            info.setRelation("Free account");
        } else {
            info.setRelation("Distributor");
        }
        info.setCreatedAt(createdAt);
        return info;
    }

    public UserInfoDisplay extractParentInfo(){
        UserInfoDisplay info = new UserInfoDisplay(parent);
        info.setRelation("Provider");
        info.setCreatedAt(createdAt);

        return info;
    }
}
