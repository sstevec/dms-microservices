package com.example.dms.service;

import com.example.dms.entity.Notification;
import com.example.dms.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository repository;

    public List<Notification> getNotifications(UUID userId, Boolean isRead) {
        if (isRead != null) {
            return repository.findByUserIdAndIsRead(userId, isRead);
        }
        return repository.findByUserId(userId);
    }

    public void saveNotification(Notification notification) {
        repository.save(notification);
    }

    public void markAsRead(UUID notificationId) {
        Notification notification = repository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        repository.save(notification);
    }
}
