package com.example.dms.service;

import com.example.dms.entity.User.AccountType;
import com.example.dms.entity.User;
import com.example.dms.entity.UserRelationship;
import com.example.dms.repository.UserRepository;
import com.example.dms.util.MessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private MessageUtil messageUtil;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRelationshipService userRelationshipService;

    @Value("${app.default.max-free-accounts}")
    private int defaultMaxFreeAccounts;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserRelationshipService userRelationshipService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRelationshipService = userRelationshipService;
    }

    @Transactional(readOnly = true)
    public void sendInvitationNotification(String fromUsername, String toUsername) throws Exception {
        User fromUser = userRepository.findByEmail(fromUsername).orElseThrow(() -> new UsernameNotFoundException(fromUsername));
        User toUser = userRepository.findByEmail(toUsername).orElseThrow(() -> new UsernameNotFoundException(toUsername));

        if (userRelationshipService.checkRelationship(fromUser.getId(), toUser.getId())) {
            throw new Exception("Relationship already exist");
        }

        String messageContent = "You have received an invitation!";
        String messageType = "Invitation";
        String payload = "{\"fromUserEmail\":\"" + fromUsername + "\"}";

        // Assemble the message
        String message = messageUtil.assembleMessage(toUser.getId(), messageContent, messageType, payload);

        // Send the message to Kafka
        kafkaTemplate.send("user-events", message);
    }

    @Transactional
    public boolean verifyPassword(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));
        return passwordEncoder.matches(password, user.getPassword());
    }

    @Transactional
    public User getUserInfo(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));
        return user;
    }


    @Transactional
    public User addFreeUser(UUID parentId, String email, String name, String password) {
        User parentUser = userRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent account not found"));

        if (parentUser.getAccountType() != AccountType.PAID) {
            throw new IllegalStateException("Only paid accounts can create free accounts");
        }

        if (!parentUser.canCreateFreeAccount()) {
            throw new IllegalStateException("Max free accounts limit reached for this parent account");
        }

        User freeUser = new User();
        freeUser.setEmail(email);
        freeUser.setName(name);
        freeUser.setPassword(passwordEncoder.encode(password)); // Hash password
        freeUser.setAccountType(AccountType.FREE);

        parentUser.addFreeAccount(freeUser);

        userRepository.save(freeUser);
        userRelationshipService.addRelationship(parentUser, freeUser);

        return freeUser;
    }

    @Transactional
    public User addPaidUser(String email, String name, String password) {
        User paidUser = new User();
        paidUser.setEmail(email);
        paidUser.setName(name);
        paidUser.setPassword(passwordEncoder.encode(password)); // Hash password
        paidUser.setAccountType(AccountType.PAID);
        paidUser.setMaxFreeAccounts(defaultMaxFreeAccounts);

        return userRepository.save(paidUser);
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getAccountType() == AccountType.FREE && user.getParentAccount() != null) {
            throw new IllegalStateException("Free accounts cannot be deleted");
        }

        userRepository.delete(user);
    }

    @Transactional
    public void deleteUsers(List<UUID> ids) {
        List<User> users = userRepository.findByIdIn(ids);

        for (User user : users) {
            if (user.getAccountType() == AccountType.FREE && user.getParentAccount() != null) {
                throw new IllegalStateException("Free accounts cannot be deleted");
            }
        }

        userRepository.deleteAll(users);
    }

    @Transactional
    public User updateFreeUser(UUID parentId, UUID id, String email, String password, String name) {
        User freeUser = userRepository.findByIdAndParentAccountId(id, parentId)
                .orElseThrow(() -> new IllegalArgumentException("Free account not found or not associated with the parent account"));

        if (email != null) freeUser.setEmail(email);
        if (password != null) freeUser.setPassword(passwordEncoder.encode(password)); // Hash password
        if (name != null) freeUser.setName(name);

        return userRepository.save(freeUser);
    }

    @Transactional(readOnly = true)
    public List<User> getFreeAccountsByParent(UUID parentId) {
        return userRepository.findByParentAccountId(parentId);
    }

    @Transactional(readOnly = true)
    public User getParentAccountByFreeUser(UUID freeUserId) {
        User freeUser = userRepository.findById(freeUserId)
                .orElseThrow(() -> new IllegalArgumentException("Free account not found"));

        if (freeUser.getParentAccount() == null) {
            throw new IllegalStateException("This account does not have a parent account");
        }

        return freeUser.getParentAccount();
    }
}
