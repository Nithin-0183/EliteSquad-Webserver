package com.example.userserver.service;

import com.example.userserver.model.Notification;
import com.example.userserver.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSendNotification() {
        Notification notification = new Notification();
        notification.setUserId("user1");
        notification.setMessage("Test message");
        notification.setTimestamp(LocalDateTime.now());

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        Mono<Notification> result = notificationService.sendNotification(notification);

        StepVerifier.create(result)
                .expectNextMatches(savedNotification -> savedNotification.getUserId().equals("user1") &&
                        savedNotification.getMessage().equals("Test message"))
                .verifyComplete();
    }

    @Test
    public void testGetNotificationHistory() {
        Notification notification1 = new Notification();
        notification1.setUserId("user1");
        notification1.setMessage("Test message 1");
        notification1.setTimestamp(LocalDateTime.now());

        Notification notification2 = new Notification();
        notification2.setUserId("user1");
        notification2.setMessage("Test message 2");
        notification2.setTimestamp(LocalDateTime.now());

        List<Notification> notifications = Arrays.asList(notification1, notification2);

        when(notificationRepository.findByUserId("user1")).thenReturn(notifications);

        Flux<Notification> result = notificationService.getNotificationHistory("user1");

        StepVerifier.create(result)
                .expectNext(notification1)
                .expectNext(notification2)
                .verifyComplete();
    }

    // @Test
    // public void testUpdateNotification() {
    //     Notification notification = new Notification();
    //     notification.setId(1L);
    //     notification.setUserId("user1");
    //     notification.setMessage("Updated message");
    //     notification.setTimestamp(LocalDateTime.now());

    //     when(notificationRepository.findById(1L)).thenReturn(java.util.Optional.of(notification));
    //     when(notificationRepository.save(notification)).thenReturn(notification);

    //     Mono<Notification> result = notificationService.updateNotification(1L, notification);

    //     StepVerifier.create(result)
    //             .expectNextMatches(updatedNotification -> updatedNotification.getMessage().equals("Updated message"))
    //             .verifyComplete();
    // }

    // @Test
    // public void testDeleteNotification() {
    //     Notification notification = new Notification();
    //     notification.setId(1L);
    //     notification.setUserId("user1");
    //     notification.setMessage("Notification to be deleted");
    //     notification.setTimestamp(LocalDateTime.now());

    //     when(notificationRepository.findById(1L)).thenReturn(java.util.Optional.of(notification));
    //     doNothing().when(notificationRepository).delete(notification);

    //     Mono<Void> result = notificationService.deleteNotification(1L);

    //     StepVerifier.create(result)
    //             .verifyComplete();

    //     verify(notificationRepository, times(1)).delete(notification);
    // }
}
