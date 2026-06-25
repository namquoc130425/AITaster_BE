package com.example.AiTaster.repository;

import com.example.AiTaster.entity.Conversation;
import com.example.AiTaster.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepo extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByConversationId(Long conversationId);

    List<Conversation> findByClientOrExpert(User client, User expert);

    Optional<Conversation> findByClientAndExpertAndProjectId(
            User client,
            User expert,
            Long projectId
    );
}