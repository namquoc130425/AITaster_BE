package com.example.AiTaster.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long categoryId;

    String categoryName;


    String slug; // slug là tên không dấu , viết thường , cách nhau bằng dấu gạch ngang , dùng để hiển thị trên URL


    @Column(columnDefinition = "TEXT")
    String description;

    // mối quan hệ với ExpertService : 1 category có nhiều ExpertService
    //                                 1 ExpertService chỉ đc 1 category
}
