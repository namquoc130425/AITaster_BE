package com.example.AiTaster.dto.request.AdminDashboard;

import com.example.AiTaster.dto.request.PageRequest;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminRecentAccountFilterRequest extends PageRequest {

    SubAdminRecentAccountFilterRequest filter;
}