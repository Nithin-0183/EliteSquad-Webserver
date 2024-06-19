package com.example.userserver.service;

import com.example.userserver.model.ChatMessage;
import com.example.userserver.repository.ChatMessageRepository;
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
import static org.mockito.Mockito.*;

public class ChatServiceTest {

    @InjectMocks
    private ChatService chatService;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSendMessage() {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId("user1");
        chatMessage.setReceiverId("user2");
        chatMessage.setContent("Hello, World!");
        chatMessage.setTimestamp(LocalDateTime.now());

        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(chatMessage);

        Mono<ChatMessage> result = chatService.sendMessage(chatMessage);

        StepVerifier.create(result)
                .expectNextMatches(savedMessage -> savedMessage.getSenderId().equals("user1") &&
                        savedMessage.getReceiverId().equals("user2") &&
                        savedMessage.getContent().equals("Hello, World!"))
                .verifyComplete();
    }

    @Test
    public void testReceiveMessages() {
        ChatMessage chatMessage1 = new ChatMessage();
        chatMessage1.setSenderId("user1");
        chatMessage1.setReceiverId("user2");
        chatMessage1.setContent("Hello, World!");
        chatMessage1.setTimestamp(LocalDateTime.now());

        ChatMessage chatMessage2 = new ChatMessage();
        chatMessage2.setSenderId("user1");
        chatMessage2.setReceiverId("user2");
        chatMessage2.setContent("How are you?");
        chatMessage2.setTimestamp(LocalDateTime.now());

        List<ChatMessage> messages = Arrays.asList(chatMessage1, chatMessage2);

        when(chatMessageRepository.findByReceiverId("user2")).thenReturn(messages);

        Flux<ChatMessage> result = chatService.receiveMessages("user2");

        StepVerifier.create(result)
                .expectNext(chatMessage1)
                .expectNext(chatMessage2)
                .verifyComplete();
    }

    @Test
    public void testGetChatHistory() {
        ChatMessage chatMessage1 = new ChatMessage();
        chatMessage1.setSenderId("user1");
        chatMessage1.setReceiverId("user2");
        chatMessage1.setConversationId("conv1");
        chatMessage1.setContent("Hello, World!");
        chatMessage1.setTimestamp(LocalDateTime.now());

        ChatMessage chatMessage2 = new ChatMessage();
        chatMessage2.setSenderId("user2");
        chatMessage2.setReceiverId("user1");
        chatMessage2.setConversationId("conv1");
        chatMessage2.setContent("Hi!");
        chatMessage2.setTimestamp(LocalDateTime.now());

        List<ChatMessage> messages = Arrays.asList(chatMessage1, chatMessage2);

        when(chatMessageRepository.findByConversationId("conv1")).thenReturn(messages);

        Flux<ChatMessage> result = chatService.getChatHistory("conv1");

        StepVerifier.create(result)
                .expectNext(chatMessage1)
                .expectNext(chatMessage2)
                .verifyComplete();
    }

    // @Test
    // public void testUpdateMessage() {
    //     ChatMessage chatMessage = new ChatMessage();
    //     chatMessage.setId(1L);
    //     chatMessage.setSenderId("user1");
    //     chatMessage.setReceiverId("user2");
    //     chatMessage.setContent("Updated Message");
    //     chatMessage.setTimestamp(LocalDateTime.now());

    //     when(chatMessageRepository.findById(1L)).thenReturn(java.util.Optional.of(chatMessage));
    //     when(chatMessageRepository.save(chatMessage)).thenReturn(chatMessage);

    //     Mono<ChatMessage> result = chatService.updateMessage(1L, chatMessage);

    //     StepVerifier.create(result)
    //             .expectNextMatches(updatedMessage -> updatedMessage.getContent().equals("Updated Message"))
    //             .verifyComplete();
    // }

    // @Test
    // public void testDeleteMessage() {
    //     ChatMessage chatMessage = new ChatMessage();
    //     chatMessage.setId(1L);
    //     chatMessage.setSenderId("user1");
    //     chatMessage.setReceiverId("user2");
    //     chatMessage.setContent("Message to be deleted");
    //     chatMessage.setTimestamp(LocalDateTime.now());

    //     when(chatMessageRepository.findById(1L)).thenReturn(java.util.Optional.of(chatMessage));
    //     doNothing().when(chatMessageRepository).delete(chatMessage);

    //     Mono<Void> result = chatService.deleteMessage(1L);

    //     StepVerifier.create(result)
    //             .verifyComplete();

    //     verify(chatMessageRepository, times(1)).delete(chatMessage);
    // }
}
