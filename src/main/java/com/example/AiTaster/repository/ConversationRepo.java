package com.example.AiTaster.repository;

import com.example.AiTaster.entity.Conversation;
import com.example.AiTaster.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepo extends JpaRepository<Conversation, Long> {

    boolean existsByExpertApplication_ApplicationId(Long applicationId);

    Optional<Conversation> findByExpertApplication_ApplicationId(Long applicationId);

    @EntityGraph(attributePaths = {
            "expertApplication",
            "expertApplication.jobpost",
            "expertApplication.jobpost.clientProfile",
            "expertApplication.expertProfile",
            "client",
            "expert"
    })
    Optional<Conversation> findWithDetailByConversationId(Long conversationId);

    @EntityGraph(attributePaths = {
            "expertApplication",
            "expertApplication.jobpost",
            "expertApplication.jobpost.clientProfile",
            "expertApplication.expertProfile",
            "client",
            "expert"
    })
    Optional<Conversation> findWithDetailByProjectId(Long projectId);

    @EntityGraph(attributePaths = {
            "expertApplication",
            "expertApplication.jobpost",
            "client",
            "expert"
    })
    List<Conversation> findByClientOrExpertOrderByUpdateAtDesc(User client, User expert);
}
