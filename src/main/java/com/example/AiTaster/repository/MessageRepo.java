package com.example.AiTaster.repository;

import com.example.AiTaster.entity.Conversation;
import com.example.AiTaster.entity.Message;
import com.example.AiTaster.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageRepo
        extends JpaRepository<Message, Long> {

    Optional<Message> findByMessageId(
            Long messageId
    );

    boolean existsByConversation(
            Conversation conversation
    );

    List<Message> findByConversationOrderBySendAtAsc(
            Conversation conversation
    );

    long countByConversationAndReceiverAndIsReadFalse(
            Conversation conversation,
            User receiver
    );

    @Modifying(
            flushAutomatically = true,
            clearAutomatically = true
    )
    @Query("""
            update Message m
            set m.isRead = true
            where m.conversation = :conversation
              and m.receiver = :receiver
              and m.isRead = false
            """)
    int markConversationAsRead(
            @Param("conversation")
            Conversation conversation,

            @Param("receiver")
            User receiver
    );
}
