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
<<<<<<< HEAD
=======
@Builder
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long categoryId;

    String categoryName;


    String slug; // slug là tên không dấu , viết thường , cách nhau bằng dấu gạch ngang , dùng để hiển thị trên URL


    @Column(columnDefinition = "TEXT")
    String description;

    @CreationTimestamp
    LocalDateTime createAt;

    @UpdateTimestamp
    LocalDateTime updateAt;


    // cascade all là xóa tk cha thì tk con sẽ xóa theo
    @OneToMany(mappedBy = "category" ,cascade = CascadeType.ALL)
    List<ExpertService> expertServices;
<<<<<<< HEAD
=======

    @OneToMany(mappedBy = "category")
    List<ExpertProfile> expertProfiles;
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
}


