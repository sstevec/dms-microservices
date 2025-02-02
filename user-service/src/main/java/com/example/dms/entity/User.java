package com.example.dms.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // Store the hashed password

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    @Column(nullable = false)
    private int maxFreeAccounts = 0; // Default limit for paid users

    @OneToMany(mappedBy = "parentAccount", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<User> freeAccounts = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "parent_account_id")
    private User parentAccount; // For free accounts, points to the paid account that created it

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum AccountType {
        PAID,
        FREE
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public int getMaxFreeAccounts() {
        return maxFreeAccounts;
    }

    public void setMaxFreeAccounts(int maxFreeAccounts) {
        this.maxFreeAccounts = maxFreeAccounts;
    }

    public List<User> getFreeAccounts() {
        return freeAccounts;
    }

    public void setFreeAccounts(List<User> freeAccounts) {
        this.freeAccounts = freeAccounts;
    }

    public User getParentAccount() {
        return parentAccount;
    }

    public void setParentAccount(User parentAccount) {
        this.parentAccount = parentAccount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Business logic
    public boolean canCreateFreeAccount() {
        return accountType == AccountType.PAID && freeAccounts.size() < maxFreeAccounts;
    }

    public void addFreeAccount(User freeAccount) {
        if (canCreateFreeAccount()) {
            freeAccounts.add(freeAccount);
            freeAccount.setParentAccount(this);
        } else {
            throw new IllegalStateException("Max free accounts limit reached.");
        }
    }

    public void hideSensitiveField() {
        this.password = "";
        this.parentAccount = null;
        this.freeAccounts = null;
        this.maxFreeAccounts = 0;
    }
}


