package com.example.AiTaster.mapper;

import com.example.AiTaster.dto.response.DisputeResponse;
import com.example.AiTaster.entity.Deliverable;
import com.example.AiTaster.entity.Dispute;
import com.example.AiTaster.entity.ProjectEscrow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DisputeMapper {

    @Mapping(target = "projectId", source = "dispute.project.projectId")
    @Mapping(target = "projectTitle", source = "dispute.project.title")
    @Mapping(target = "deliverableId", source = "dispute.deliverable.deliverableId")
    @Mapping(target = "disputedStep", source = "dispute.deliverable.step")
    @Mapping(target = "disputedStepTitle", expression = "java(getDisputedStepTitle(dispute.getDeliverable()))")
    @Mapping(target = "disputedDeliverableVersion", source = "dispute.deliverable.version")
    @Mapping(target = "disputedDeliverableSubmittedAt", source = "dispute.deliverable.submittedAt")
    @Mapping(target = "reporterId", source = "dispute.reporter.userId")
    @Mapping(target = "reporterName", source = "dispute.reporter.fullName")
    @Mapping(target = "reportedAgainstId", source = "dispute.reportedAgainst.userId")
    @Mapping(target = "reportedAgainstName", source = "dispute.reportedAgainst.fullName")
    @Mapping(target = "escrowHeldAmount", source = "escrow.heldAmount")
    @Mapping(target = "createdAt", source = "dispute.createAt")
    @Mapping(target = "resolvedAt", source = "dispute.resolvedAt")
    @Mapping(target = "escrowStatus", ignore = true)
    @Mapping(target = "projectOutcome", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "deliverables", ignore = true)
    DisputeResponse toResponse(Dispute dispute, ProjectEscrow escrow);

    default String getDisputedStepTitle(Deliverable deliverable) {
        if (deliverable == null || deliverable.getStep() == null) {
            return null;
        }

        return deliverable.getStep().getTitle();
    }
}
