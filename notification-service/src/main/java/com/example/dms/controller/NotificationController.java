package com.example.dms.controller;

import com.example.dms.entity.Notification;
import com.example.dms.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public List<Notification> getNotifications(@RequestParam UUID userId, @RequestParam(required = false) Boolean isRead) {
        return notificationService.getNotifications(userId, isRead);
    }

    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable String id) {
        notificationService.markAsRead(UUID.fromString(id));
    }
}
