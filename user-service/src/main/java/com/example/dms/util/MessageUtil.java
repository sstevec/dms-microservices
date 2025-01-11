package com.example.dms.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MessageUtil {
    public String assembleMessage(UUID receiver, String messageBody, String notificationType, String payload) {
        String receiverId = receiver.toString();

        // Assemble the message
        return String.join("|", receiverId, messageBody, notificationType, payload);
    }
}
