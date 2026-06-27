package com.example.AiTaster.entity;

import com.example.AiTaster.constant.ReportStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporterId", nullable = false)
    User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportedUserId", nullable = false)
    User reportedUser;

    String reportTitle;

    String reportReason;

    @Column(columnDefinition = "TEXT")
    String reportDescription;

    String evidenceFile;

    @Enumerated(EnumType.STRING)
    ReportStatus reportStatus;

    String adminResponse;

    @CreationTimestamp
    LocalDateTime createdAt;
}