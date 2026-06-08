package com.example.AiTaster.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import org.springframework.data.annotation.Id;

public class ExpertAIService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long serviceId;

}
