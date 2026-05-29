package com.example.AiTaster.entity;

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
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long skillId;

    @Column
    String skillName;

    String slug;

    String description;

    @CreationTimestamp // tự reset time khi insert
    LocalDateTime createdAt;

    //tự cập nhật time mỗi lần update
    @UpdateTimestamp
    LocalDateTime updateAt;

    @OneToMany(mappedBy = "skill")
    List<ExpertProfileSkill> expertProfileSkill;

}
