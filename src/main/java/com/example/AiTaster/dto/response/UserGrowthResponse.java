package com.example.AiTaster.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserGrowthResponse {

    List<String> labels;

    List<Long> clients;

    List<Long> experts;
}