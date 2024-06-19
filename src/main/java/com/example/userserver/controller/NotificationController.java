package com.example.userserver.controller;

import com.example.userserver.model.Notification;
import com.example.userserver.service.NotificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // Send a new notification
    @PostMapping("/send")
    public Mono<Notification> sendNotification(@RequestBody Notification notification) {
        return notificationService.sendNotification(notification);
    }

    // Retrieve all notification history for a specific user
    @GetMapping(value = "/history/{userId}", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Notification> getNotificationHistory(@PathVariable String userId) {
        return notificationService.getNotificationHistory(userId);
    }
}