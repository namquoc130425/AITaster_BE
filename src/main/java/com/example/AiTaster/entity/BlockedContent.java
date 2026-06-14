package com.example.AiTaster.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity // Báo JPA đây là bảng DB.
@Table(name = "blocked_content") // Tên bảng trong MySQL.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockedContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long blockedContentId;
}
