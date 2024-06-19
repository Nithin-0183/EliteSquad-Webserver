package com.example.userserver.controller;

import com.example.userserver.model.ChatMessage;
import com.example.userserver.service.ChatService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    // Send a new chat message
    @PostMapping("/send")
    public Mono<ChatMessage> sendMessage(@RequestBody ChatMessage chatMessage) {
        return chatService.sendMessage(chatMessage);
    }

    // Retrieve chat messages received by a specific user
    @GetMapping(value = "/receive/{userId}", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<ChatMessage> receiveMessages(@PathVariable String userId) {
        return chatService.receiveMessages(userId);
    }

    // Retrieve chat history for a specific conversation
    @GetMapping(value = "/history/{conversationId}", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<ChatMessage> getChatHistory(@PathVariable String conversationId) {
        return chatService.getChatHistory(conversationId);
    }
}