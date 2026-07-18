package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.response.RatingResponse;
import com.example.AiTaster.entity.Rating;
import org.springframework.stereotype.Component;

@Component
public class RatingMapper {

    public RatingResponse toResponse(Rating rating) {
        if (rating == null) {
            return null;
        }

        return RatingResponse.builder()
                .ratingId(rating.getRatingId())
                .clientProfileId(rating.getClientProfile() != null
                        ? rating.getClientProfile().getClientProfileId()
                        : null)
                .clientName(rating.getClientProfile() != null && rating.getClientProfile().getUser() != null
                        ? rating.getClientProfile().getUser().getFullName()
                        : null)
                .clientAvatarUrl(rating.getClientProfile() != null && rating.getClientProfile().getUser() != null
                        ? rating.getClientProfile().getUser().getAvatarUrl()
                        : null)
                .expertProfileId(rating.getExpertProfile() != null
                        ? rating.getExpertProfile().getExpertProfileId()
                        : null)
                .expertName(rating.getExpertProfile() != null && rating.getExpertProfile().getUser() != null
                        ? rating.getExpertProfile().getUser().getFullName()
                        : null)
                .expertServiceId(rating.getExpertService() != null
                        ? rating.getExpertService().getServiceId()
                        : null)
                .expertServiceName(rating.getExpertService() != null
                        ? rating.getExpertService().getServiceName()
                        : null)
                .projectId(rating.getProject() != null
                        ? rating.getProject().getProjectId()
                        : null)
                .projectTitle(rating.getProject() != null
                        ? rating.getProject().getTitle()
                        : null)
                .targetType(rating.getTargetType())
                .rating(rating.getRating())
                .review(rating.getReview())
                .createAt(rating.getCreateAt())
                .updateAt(rating.getUpdateAt())
                .build();
    }
}
