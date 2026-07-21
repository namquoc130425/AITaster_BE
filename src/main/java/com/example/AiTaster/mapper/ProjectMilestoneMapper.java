package com.example.AiTaster.mapper;

import com.example.AiTaster.constant.MilestoneStatus;
import com.example.AiTaster.dto.response.ProjectMilestoneResponse;
import com.example.AiTaster.entity.ProjectMilestone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMilestoneMapper {
    //expressin : code java xữ lý riêng không map thẳng đc

      @Mapping(target = "currentStepTitle",expression = "java(projectMilestone.getCurrentStep()!= null ? projectMilestone.getCurrentStep().getTitle() : null)")
      @Mapping(target = "canApprove", expression = "java(canClientReview(projectMilestone))")
      @Mapping(target = "canRequestRevision", expression = "java(canClientReview(projectMilestone))")
    ProjectMilestoneResponse toResponse(ProjectMilestone projectMilestone);

    default boolean canClientReview(ProjectMilestone projectMilestone) {
        return projectMilestone != null
                && projectMilestone.getStatus() == MilestoneStatus.WAITING_CLIENT_REVIEW;
    }
}
