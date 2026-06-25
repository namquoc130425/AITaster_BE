package com.example.AiTaster.repository;

import com.example.AiTaster.entity.Conversation;
import com.example.AiTaster.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRepo extends JpaRepository<Message, Long> {

    Optional<Message> findByMessageId(Long messageId);

    List<Message> findByConversationOrderBySendAtAsc(Conversation conversation);
}