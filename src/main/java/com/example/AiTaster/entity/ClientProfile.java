package com.example.AiTaster.entity;

import com.example.AiTaster.constant.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ClientProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long clientProfileId;

    @JsonIgnore //chống vòng lặp
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    User user;

    String companyName;
    String contactName;

    @Column(columnDefinition = "TEXT")
    String description;

    String businessField;
    String address;

//    @Enumerated(EnumType.STRING)  nên bỏ vì set bên User rồi . sau này cần lấy thì join bảng để lấy ra
//    Role role;

    LocalDateTime createAt;
    LocalDateTime updateAt;

    @PrePersist
    protected void onCreate() {
        createAt = LocalDateTime.now();
        updateAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "clientProfile",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    List<JobPost> jobPosts;
}
