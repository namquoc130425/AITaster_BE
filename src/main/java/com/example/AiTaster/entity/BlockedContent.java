package com.example.AiTaster.entity;

import com.example.AiTaster.constant.BlockedContentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity // Báo JPA đây là bảng DB.
@Table(name = "blocked_content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockedContent implements Serializable {
    // implements Serializable nghĩa là class BlockContent cho phép java chuyển object thành dạng có thể lưu , truyền được ví dụ thành byte,rồi sau đó đọc ngược lại thành object
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long blockedContentId;

    @Column(nullable = false, unique = true)
    String content;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    BlockedContentType type; // KEYWORD hoặc PROMPT_INJECTION.


    @CreationTimestamp
    LocalDateTime createAt;

    @UpdateTimestamp
    LocalDateTime updateAt;


}
