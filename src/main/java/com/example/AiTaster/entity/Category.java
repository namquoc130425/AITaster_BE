package com.example.AiTaster.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

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


    // cascade all là xóa tk cha thì tk con sẽ xóa theo
    @OneToMany(mappedBy = "category" ,cascade = CascadeType.ALL)
    List<ExpertService> expertServices;
}


