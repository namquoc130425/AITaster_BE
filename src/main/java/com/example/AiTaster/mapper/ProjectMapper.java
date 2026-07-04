package com.example.AiTaster.mapper;

import com.example.AiTaster.constant.InvitationStatus;
import com.example.AiTaster.constant.MilestoneStatus;
import com.example.AiTaster.constant.MilestoneStep;
import com.example.AiTaster.constant.ProjectStatus;
import com.example.AiTaster.dto.response.ProjectCardResponse;
import com.example.AiTaster.dto.response.ProjectStepResponse;
import com.example.AiTaster.entity.Invitation;
import com.example.AiTaster.entity.Project;
import com.example.AiTaster.entity.ProjectMilestone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        imports = {InvitationStatus.class, ProjectStatus.class}
)
public interface ProjectMapper {

    @Mapping(target = "projectId", source = "project.projectId")
    @Mapping(target = "invitationId", source = "project.invitation.invitationId")
    @Mapping(target = "applicationId", source = "project.invitation.expertApplication.applicationId")
    @Mapping(target = "jobPostId", source = "project.invitation.expertApplication.jobpost.jobPostId")
    @Mapping(target = "currentUserRole", expression = "java(isClientProject ? \"CLIENT\" : \"EXPERT\")")
    @Mapping(target = "sourceType", constant = "PROJECT")
    @Mapping(target = "workflowStatus", expression = "java(project.getProjectStatus().name())")
    @Mapping(target = "title", source = "project.title")
    @Mapping(target = "description", source = "project.finalRequirementSnapshot")
    @Mapping(target = "expectedOutput", source = "project.expectedOutputSnapshot")
    @Mapping(target = "acceptanceCriteria", source = "project.acceptanceCriteriaSnapshot")
    @Mapping(target = "projectStatus", source = "project.projectStatus")
    @Mapping(target = "invitationStatus", source = "project.invitation.invitationStatus")
    @Mapping(target = "escrowStatus", expression = "java(getEscrowStatus(project.getProjectStatus()))")
    @Mapping(target = "paymentStatus", expression = "java(getPaymentStatus(project.getProjectStatus()))")
    @Mapping(target = "clientName", source = "project.invitation.expertApplication.jobpost.clientProfile.contactName")
    @Mapping(target = "companyName", source = "project.invitation.expertApplication.jobpost.clientProfile.companyName")
    @Mapping(target = "expertName", source = "project.invitation.expertApplication.expertProfile.user.fullName")
    @Mapping(target = "budget", source = "project.agreedPrice")
    @Mapping(target = "timeline", source = "project.timeline")
    @Mapping(target = "deadlineAt", source = "project.deadlineAt")
    @Mapping(target = "paymentDeadlineAt", source = "project.paymentDeadlineAt")
    @Mapping(target = "currentStepCode", expression = "java(getCurrentStepCode(project.getProjectStatus(), milestone))")
    @Mapping(target = "currentStepTitle", expression = "java(getCurrentStepTitle(project.getProjectStatus(), milestone))")
    @Mapping(target = "currentStepDescription", expression = "java(getCurrentStepDescription(project.getProjectStatus(), milestone))")
    @Mapping(target = "milestoneStatus", expression = "java(milestone != null ? milestone.getStatus().name() : null)")
    @Mapping(target = "canPayWithSepay", expression = "java(isClientProject && project.getProjectStatus() == ProjectStatus.WAITING_ESCROW)")
    @Mapping(target = "canOpenWorkspace", expression = "java(project.getProjectStatus() == ProjectStatus.ACTIVE)")
    @Mapping(target = "canViewPaymentStatus", expression = "java(isClientProject)")
    @Mapping(target = "canViewDetails", expression = "java(true)")
    @Mapping(target = "canViewSummary", expression = "java(project.getProjectStatus() == ProjectStatus.COMPLETED)")
    @Mapping(target = "canDownloadReceipt", expression = "java(isClientProject && project.getProjectStatus() == ProjectStatus.COMPLETED)")
    @Mapping(target = "steps", expression = "java(buildSteps(project.getProjectStatus(), milestone))")
    @Mapping(target = "startAt", source = "project.startAt")
    @Mapping(target = "completedAt", source = "project.completedAt")
    @Mapping(target = "createAt", source = "project.createAt")
    @Mapping(target = "updateAt", source = "project.updateAt")
    ProjectCardResponse toCardResponse(
            Project project,
            ProjectMilestone milestone,
            boolean isClientProject
    );

    @Mapping(target = "projectId", ignore = true)
    @Mapping(target = "invitationId", source = "invitation.invitationId")
    @Mapping(target = "applicationId", source = "invitation.expertApplication.applicationId")
    @Mapping(target = "jobPostId", source = "invitation.expertApplication.jobpost.jobPostId")
    @Mapping(target = "currentUserRole", expression = "java(isClientProject ? \"CLIENT\" : \"EXPERT\")")
    @Mapping(target = "sourceType", constant = "INVITATION")
    @Mapping(target = "workflowStatus", expression = "java(getWorkflowStatus(invitation.getInvitationStatus()))")
    @Mapping(target = "title", source = "invitation.projectTitle")
    @Mapping(target = "description", source = "invitation.finalRequirement")
    @Mapping(target = "expectedOutput", source = "invitation.expectedOutput")
    @Mapping(target = "acceptanceCriteria", source = "invitation.acceptanceCriteria")
    @Mapping(target = "projectStatus", expression = "java(invitation.getInvitationStatus() == InvitationStatus.ACCEPTED ? ProjectStatus.WAITING_ESCROW : null)")
    @Mapping(target = "invitationStatus", source = "invitation.invitationStatus")
    @Mapping(target = "escrowStatus", expression = "java(getEscrowStatus(invitation.getInvitationStatus()))")
    @Mapping(target = "paymentStatus", expression = "java(getPaymentStatus(invitation.getInvitationStatus()))")
    @Mapping(target = "clientName", source = "invitation.expertApplication.jobpost.clientProfile.contactName")
    @Mapping(target = "companyName", source = "invitation.expertApplication.jobpost.clientProfile.companyName")
    @Mapping(target = "expertName", source = "invitation.expertApplication.expertProfile.user.fullName")
    @Mapping(target = "budget", source = "invitation.finalOfferedPrice")
    @Mapping(target = "timeline", source = "invitation.finalTimeline")
    @Mapping(target = "deadlineAt", ignore = true)
    @Mapping(target = "paymentDeadlineAt", expression = "java(invitation.getInvitationStatus() == InvitationStatus.ACCEPTED ? invitation.getExpiresAt() : null)")
    @Mapping(target = "currentStepCode", expression = "java(getCurrentStepCode(invitation.getInvitationStatus()))")
    @Mapping(target = "currentStepTitle", expression = "java(getCurrentStepTitle(invitation.getInvitationStatus()))")
    @Mapping(target = "currentStepDescription", expression = "java(getCurrentStepDescription(invitation.getInvitationStatus()))")
    @Mapping(target = "milestoneStatus", ignore = true)
    @Mapping(target = "canPayWithSepay", expression = "java(isClientProject && invitation.getInvitationStatus() == InvitationStatus.ACCEPTED)")
    @Mapping(target = "canOpenWorkspace", expression = "java(false)")
    @Mapping(target = "canViewPaymentStatus", expression = "java(isClientProject && invitation.getInvitationStatus() == InvitationStatus.ACCEPTED)")
    @Mapping(target = "canViewDetails", expression = "java(true)")
    @Mapping(target = "canViewSummary", expression = "java(false)")
    @Mapping(target = "canDownloadReceipt", expression = "java(false)")
    @Mapping(target = "steps", expression = "java(buildInvitationSteps(invitation.getInvitationStatus()))")
    @Mapping(target = "startAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "createAt", source = "invitation.createAt")
    @Mapping(target = "updateAt", source = "invitation.updateAt")
    ProjectCardResponse toInvitationCardResponse(Invitation invitation, boolean isClientProject);

    default String getWorkflowStatus(InvitationStatus status) {
        return switch (status) {
            case PENDING -> "WAITING_EXPERT_ACCEPT";
            case ACCEPTED -> "WAITING_ESCROW";
            case PAYMENT_EXPIRED -> "PAYMENT_EXPIRED";
            case REJECTED -> "REJECTED";
            case EXPIRED -> "EXPIRED";
        };
    }

    default String getEscrowStatus(InvitationStatus status) {
        return switch (status) {
            case ACCEPTED -> "UNPAID";
            case PAYMENT_EXPIRED -> "EXPIRED";
            case PENDING -> "WAITING_EXPERT";
            case REJECTED -> "REJECTED";
            case EXPIRED -> "EXPIRED";
        };
    }

    default String getPaymentStatus(InvitationStatus status) {
        return switch (status) {
            case ACCEPTED -> "UNPAID";
            case PAYMENT_EXPIRED -> "EXPIRED";
            case PENDING -> "WAITING_EXPERT_ACCEPT";
            case REJECTED -> "REJECTED";
            case EXPIRED -> "EXPIRED";
        };
    }

    default String getCurrentStepCode(InvitationStatus status) {
        return switch (status) {
            case PENDING -> "WAITING_EXPERT_ACCEPT";
            case ACCEPTED -> "WAITING_ESCROW";
            case PAYMENT_EXPIRED -> "PAYMENT_EXPIRED";
            case REJECTED -> "REJECTED";
            case EXPIRED -> "EXPIRED";
        };
    }

    default String getCurrentStepTitle(InvitationStatus status) {
        return switch (status) {
            case PENDING -> "Waiting for expert acceptance";
            case ACCEPTED -> "Waiting for escrow payment";
            case PAYMENT_EXPIRED -> "Payment expired";
            case REJECTED -> "Invitation rejected";
            case EXPIRED -> "Invitation expired";
        };
    }

    default String getCurrentStepDescription(InvitationStatus status) {
        return switch (status) {
            case PENDING -> "The invitation has been sent and is waiting for the expert to accept.";
            case ACCEPTED -> "The expert accepted. Client payment is required before the project starts.";
            case PAYMENT_EXPIRED -> "The payment window expired before escrow was funded.";
            case REJECTED -> "The expert rejected this invitation.";
            case EXPIRED -> "The expert did not respond before the invitation expired.";
        };
    }

    default List<ProjectStepResponse> buildInvitationSteps(InvitationStatus status) {
        if (status == InvitationStatus.ACCEPTED) {
            return List.of(
                    step("EXPERT_ACCEPT", "Expert Accept", "DONE"),
                    step("ESCROW_PAYMENT", "Escrow Payment", "CURRENT"),
                    step("PROJECT_START", "Project Start", "LOCKED")
            );
        }

        if (status == InvitationStatus.PENDING) {
            return List.of(
                    step("EXPERT_ACCEPT", "Expert Accept", "CURRENT"),
                    step("ESCROW_PAYMENT", "Escrow Payment", "LOCKED"),
                    step("PROJECT_START", "Project Start", "LOCKED")
            );
        }

        return List.of(
                step("EXPERT_ACCEPT", "Expert Accept", "LOCKED"),
                step("ESCROW_PAYMENT", "Escrow Payment", "LOCKED"),
                step("PROJECT_START", "Project Start", "LOCKED")
        );
    }

    default String getEscrowStatus(ProjectStatus status) {
        return switch (status) {
            case WAITING_ESCROW -> "UNPAID";
            case ACTIVE -> "HELD";
            case COMPLETED -> "RELEASED";
            case CANCELED -> "CANCELED";
        };
    }

    default String getPaymentStatus(ProjectStatus status) {
        return getEscrowStatus(status);
    }

    default String getCurrentStepCode(ProjectStatus projectStatus, ProjectMilestone milestone) {
        if (projectStatus == ProjectStatus.WAITING_ESCROW) {
            return "WAITING_ESCROW";
        }
        if (projectStatus == ProjectStatus.COMPLETED) {
            return "COMPLETED";
        }
        if (projectStatus == ProjectStatus.CANCELED) {
            return "CANCELED";
        }
        if (milestone == null || milestone.getCurrentStep() == null) {
            return MilestoneStep.DOCUMENT.name();
        }
        return milestone.getCurrentStep().name();
    }

    default String getCurrentStepTitle(ProjectStatus projectStatus, ProjectMilestone milestone) {
        if (projectStatus == ProjectStatus.WAITING_ESCROW) {
            return "Waiting for escrow payment";
        }
        if (projectStatus == ProjectStatus.COMPLETED) {
            return "All milestones completed";
        }
        if (projectStatus == ProjectStatus.CANCELED) {
            return "Project canceled";
        }
        if (milestone == null || milestone.getCurrentStep() == null) {
            return "Document";
        }
        return milestone.getCurrentStep().getTitle();
    }

    default String getCurrentStepDescription(ProjectStatus projectStatus, ProjectMilestone milestone) {
        if (projectStatus == ProjectStatus.WAITING_ESCROW) {
            return "Client payment is required before the project starts.";
        }
        if (projectStatus == ProjectStatus.COMPLETED) {
            return "All project milestones have been approved.";
        }
        if (projectStatus == ProjectStatus.CANCELED) {
            return "This project is no longer active.";
        }
        if (milestone == null || milestone.getStatus() == null) {
            return "Project workspace is ready.";
        }

        return switch (milestone.getStatus()) {
            case WAITING_EXPERT_SUBMIT -> "Waiting for expert to submit deliverables.";
            case WAITING_CLIENT_REVIEW -> "Waiting for client review.";
            case REVISION_REQUESTED -> "Client requested revisions.";
            case COMPLETED -> "All milestones completed.";
        };
    }

    default List<ProjectStepResponse> buildSteps(ProjectStatus projectStatus, ProjectMilestone milestone) {
        if (projectStatus == ProjectStatus.CANCELED) {
            return List.of(
                    step(MilestoneStep.DOCUMENT, "LOCKED"),
                    step(MilestoneStep.SOURCE_CODE, "LOCKED"),
                    step(MilestoneStep.FINAL_CONFIRMATION, "LOCKED")
            );
        }

        if (projectStatus == ProjectStatus.WAITING_ESCROW || milestone == null) {
            return List.of(
                    step(MilestoneStep.DOCUMENT, "CURRENT"),
                    step(MilestoneStep.SOURCE_CODE, "LOCKED"),
                    step(MilestoneStep.FINAL_CONFIRMATION, "LOCKED")
            );
        }

        if (projectStatus == ProjectStatus.COMPLETED || milestone.getStatus() == MilestoneStatus.COMPLETED) {
            return List.of(
                    step(MilestoneStep.DOCUMENT, "DONE"),
                    step(MilestoneStep.SOURCE_CODE, "DONE"),
                    step(MilestoneStep.FINAL_CONFIRMATION, "DONE")
            );
        }

        MilestoneStep currentStep = milestone.getCurrentStep();

        return List.of(
                step(MilestoneStep.DOCUMENT, getStepStatus(MilestoneStep.DOCUMENT, currentStep, milestone)),
                step(MilestoneStep.SOURCE_CODE, getStepStatus(MilestoneStep.SOURCE_CODE, currentStep, milestone)),
                step(MilestoneStep.FINAL_CONFIRMATION, getStepStatus(MilestoneStep.FINAL_CONFIRMATION, currentStep, milestone))
        );
    }

    default String getStepStatus(
            MilestoneStep step,
            MilestoneStep currentStep,
            ProjectMilestone milestone
    ) {
        if (step == MilestoneStep.DOCUMENT && milestone.getStep1ApprovedAt() != null) {
            return "DONE";
        }
        if (step == MilestoneStep.SOURCE_CODE && milestone.getStep2ApprovedAt() != null) {
            return "DONE";
        }
        if (step == MilestoneStep.FINAL_CONFIRMATION && milestone.getFinalApprovedAt() != null) {
            return "DONE";
        }
        if (step == currentStep) {
            return "CURRENT";
        }
        return "LOCKED";
    }

    default ProjectStepResponse step(MilestoneStep step, String status) {
        return step(step.name(), step.getTitle(), status);
    }

    default ProjectStepResponse step(String code, String label, String status) {
        return ProjectStepResponse.builder()
                .code(code)
                .label(label)
                .status(status)
                .build();
    }
}
