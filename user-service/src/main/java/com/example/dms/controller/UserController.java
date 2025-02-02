package com.example.dms.controller;

import com.example.dms.entity.User;
import com.example.dms.entity.display.UserInfoDisplay;
import com.example.dms.service.UserRelationshipService;
import com.example.dms.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRelationshipService userRelationshipService;

    public UserController(UserService userService, UserRelationshipService userRelationshipService) {
        this.userService = userService;
        this.userRelationshipService = userRelationshipService;
    }

    @PostMapping("/add-free-user")
    public ResponseEntity<String> addFreeUser(@RequestParam String name, @RequestParam String password, @RequestParam String email, @RequestParam UUID parentAccountId) {
        User user = userService.addFreeUser(parentAccountId, email, name, password);
        return ResponseEntity.ok(user.getId().toString());
    }

    @PutMapping("/modify-user/{accountId}")
    public ResponseEntity<String> modifyUser(@PathVariable UUID accountId, @RequestParam(required = false) String name, @RequestParam(required = false) String password, @RequestParam(required = false) String email) {
        User user = userService.updateFreeUser(accountId, accountId, email, password, name);
        return ResponseEntity.ok(user.getId().toString());
    }

    @GetMapping("/free-accounts/{accountId}")
    public ResponseEntity<List<User>> getFreeAccounts(@PathVariable UUID accountId) {
        List<User> freeAccounts = userService.getFreeAccountsByParent(accountId);
        freeAccounts.forEach(User::hideSensitiveField);
        return ResponseEntity.ok(freeAccounts);
    }

    @PostMapping("/account-info")
    public ResponseEntity<User> getAccountInfo(@RequestParam String email) {
        User user = userService.getUserInfo(email);
        user.hideSensitiveField();
        return ResponseEntity.ok(user);
    }

    @PostMapping("/send-invitation")
    public ResponseEntity<String> sendInvitation(@RequestParam String fromUserEmail, @RequestParam String toUserEmail) {
        try {
            userService.sendInvitationNotification(fromUserEmail, toUserEmail);
            return ResponseEntity.ok("Invitation sent");
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/accept-invitation")
    public ResponseEntity<String> acceptInvitation(@RequestParam String providerEmail, @RequestParam String userId) {
        try {
            userRelationshipService.addRelationship(providerEmail, UUID.fromString(userId));
            return ResponseEntity.ok("Link Build Success");
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/delete-link")
    public ResponseEntity<String> deleteLink(@RequestParam String parentId, @RequestParam String childId) {
        try {
            userRelationshipService.deleteRelationship(UUID.fromString(parentId), UUID.fromString(childId));
            return ResponseEntity.ok("Link Deleted Successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/get-all-linked-accounts")
    public ResponseEntity<List<UserInfoDisplay>> getAccountInfo(@RequestParam String userId, @RequestParam boolean includeFree, @RequestParam boolean includeDistributor, @RequestParam boolean includeProvider) {
        List<UserInfoDisplay> info = userRelationshipService.getLinks(UUID.fromString(userId), includeFree, includeDistributor, includeProvider);
        return ResponseEntity.ok(info);
    }
}
