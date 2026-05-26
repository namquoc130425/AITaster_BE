package com.example.AiTaster.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExpertProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long expertProfileId;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    User user;

    @Column(columnDefinition = "TEXT")
    String bio;

    String category;

    String skills;

    String yearOfExperience;

    BigDecimal rating;

    Integer completedProjects;

    String portfolioUrl;

    LocalDateTime createAt;

    LocalDateTime updateAt;

    // chạy trước khi INSERT dữ liệu lần đầu vào database
    @PrePersist
    protected void onCreate() {
        // thời gian tạo tài khoản/profile
        createAt = LocalDateTime.now();

        // lần đầu tạo thì updateAt cũng chính là thời gian tạo
        updateAt = LocalDateTime.now();

        // nếu chưa có rating
        // expert mới đăng ký sẽ mặc định = 0.0
        if (rating == null) {
            rating = BigDecimal.ZERO;
        }

        // nếu chưa có project hoàn thành
        // expert mới sẽ mặc định = 0
        if (completedProjects == null) {
            completedProjects = 0;
        }
    }
    // chạy trước khi UPDATE dữ liệu
    @PreUpdate
    protected void onUpdate() {

        // mỗi lần chỉnh sửa profile
        // tự động cập nhật thời gian sửa gần nhất
        updateAt = LocalDateTime.now();
    }
}
