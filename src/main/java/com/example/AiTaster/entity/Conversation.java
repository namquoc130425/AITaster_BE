package com.example.AiTaster.entity;

import com.example.AiTaster.constant.ConversationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long conversationId;

    // Project entity tạo sau, tạm lưu projectId dạng Long
    Long projectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_id", nullable = false)
    User expert;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ConversationType conversationType;

    @CreationTimestamp
    LocalDateTime createAt;

    @UpdateTimestamp
    LocalDateTime updateAt;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Message> messages;
}