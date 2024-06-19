package com.example.userserver.repository;

import com.example.userserver.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByReceiverId(String receiverId);
    List<ChatMessage> findByConversationId(String conversationId);
}
