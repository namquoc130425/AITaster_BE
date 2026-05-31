package com.example.AiTaster.entity;

import com.example.AiTaster.constant.JobpostStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long jobPostId;

    @Column(nullable = false)
    String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    String requirementDescription; // Mô tả yêu cầu dự án

    @Column(columnDefinition = "TEXT")
    String businessGoal; // Mục tiêu kinh doanh

    @Column(columnDefinition = "TEXT") // Text dài
    String mainFeatures; // Chức năng chính

//    @Enumerated(EnumType.STRING) // Lưu enum dạng chữ
//    @Column(nullable = false, length = 50) // Bắt buộc
//    TargetUsers targetUsers; // Nhóm người dùng mục tiêu

    @Column(columnDefinition = "TEXT") // Lưu text tạm thời vì bảng Skill chưa xong
    String requiredSkills; // Skill AI gợi ý, ví dụ React, Spring Boot, AI Integration

    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal budgets; // Ngân sách dự kiến

    @Column(nullable = false, length = 100)
    String timeLine; // Thời gian thực hiện dự kiến

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    JobpostStatus jobPostStatus;

    @CreationTimestamp
    @Column(nullable = false)
    LocalDateTime createAt;

    @UpdateTimestamp
    @Column(nullable = false)
    LocalDateTime updateAt;

 @ManyToOne(fetch = FetchType.LAZY) // NHIỀU JobPost thuộc về 1 client
 @JoinColumn(name = "clientprofile_id",nullable = false)
    ClientProfile clientProfile;


}
