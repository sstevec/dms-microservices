package com.example.dms.service;

import com.example.dms.entity.Notification;
import com.example.dms.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EventListenerService {

    @Autowired
    private NotificationRepository notificationRepository;

    @KafkaListener(topics = "user-events", groupId = "notification-service")
    public void handleUserEvent(String message) {
        // Decode the message
        String[] parts = message.split("\\|", 4); // Split into 4 parts: receiverId, messageContent, messageType, payload

        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid message format");
        }

        UUID receiverId = UUID.fromString(parts[0]);
        String messageContent = parts[1];
        String messageType = parts[2];
        String payload = parts[3];

        // Create and save the notification
        Notification notification = new Notification();
        notification.setUserId(receiverId);
        notification.setMessage(messageContent);
        notification.setType(messageType);
        notification.setPayload(payload);

        notificationRepository.save(notification);
    }
}
