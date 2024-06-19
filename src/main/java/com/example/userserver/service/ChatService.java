package com.example.userserver.service;

import com.example.userserver.model.ChatMessage;
import com.example.userserver.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    // Send a new chat message
    public Mono<ChatMessage> sendMessage(ChatMessage chatMessage) {
        chatMessage.setTimestamp(LocalDateTime.now());
        return Mono.fromCallable(() -> chatMessageRepository.save(chatMessage));
    }

    // Retrieve chat messages received by a specific user
    public Flux<ChatMessage> receiveMessages(String userId) {
        return Mono.fromCallable(() -> chatMessageRepository.findByReceiverId(userId))
                   .flatMapMany(Flux::fromIterable);
    }

    // Retrieve chat history for a specific conversation
    public Flux<ChatMessage> getChatHistory(String conversationId) {
        return Mono.fromCallable(() -> chatMessageRepository.findByConversationId(conversationId))
                   .flatMapMany(Flux::fromIterable);
    }
}

