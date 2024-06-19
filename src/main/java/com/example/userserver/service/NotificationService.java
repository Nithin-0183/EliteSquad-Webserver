package com.example.userserver.service;

import com.example.userserver.model.Notification;
import com.example.userserver.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // Send a new notification
    public Mono<Notification> sendNotification(Notification notification) {
        notification.setTimestamp(LocalDateTime.now());
        return Mono.fromCallable(() -> notificationRepository.save(notification));
    }

    // Retrieve all notification history for a specific user
    public Flux<Notification> getNotificationHistory(String userId) {
        return Mono.fromCallable(() -> notificationRepository.findByUserId(userId))
                   .flatMapMany(Flux::fromIterable);
    }
}
