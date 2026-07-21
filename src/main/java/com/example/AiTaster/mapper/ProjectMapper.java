package com.example.AiTaster.mapper;

import com.example.AiTaster.constant.InvitationStatus;
import com.example.AiTaster.constant.MilestoneStatus;
import com.example.AiTaster.constant.MilestoneStep;
import com.example.AiTaster.constant.ProjectStatus;
import com.example.AiTaster.dto.response.ProjectCardResponse;
import com.example.AiTaster.dto.response.ProjectStepResponse;
import com.example.AiTaster.entity.ExpertApplication;
import com.example.AiTaster.entity.Invitation;
import com.example.AiTaster.entity.JobPost;
import com.example.AiTaster.entity.Project;
import com.example.AiTaster.entity.ProjectMilestone;
<<<<<<< HEAD
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProjectMapper {

    public ProjectCardResponse toCardResponse(
=======
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    default ProjectCardResponse toCardResponse(
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
            Project project,
            ProjectMilestone milestone,
            boolean isClientProject
    ) {
        Invitation invitation = project.getInvitation();
        ExpertApplication application = invitation.getExpertApplication();
        JobPost jobPost = application.getJobpost();
        ProjectStatus projectStatus = project.getProjectStatus();

        return ProjectCardResponse.builder()
                .projectId(project.getProjectId())
                .invitationId(invitation.getInvitationId())
                .applicationId(application.getApplicationId())
                .jobPostId(jobPost.getJobPostId())
                .currentUserRole(isClientProject ? "CLIENT" : "EXPERT")
                .sourceType("PROJECT")
                .workflowStatus(projectStatus.name())
                .title(project.getTitle())
                .description(project.getFinalRequirementSnapshot())
                .expectedOutput(project.getExpectedOutputSnapshot())
                .acceptanceCriteria(project.getAcceptanceCriteriaSnapshot())
                .projectStatus(projectStatus)
                .invitationStatus(invitation.getInvitationStatus())
                .escrowStatus(getEscrowStatus(projectStatus))
                .paymentStatus(getPaymentStatus(projectStatus))
                .clientName(jobPost.getClientProfile().getContactName())
<<<<<<< HEAD
                .companyName(jobPost.getClientProfile().getCompanyName())
                .expertName(application.getExpertProfile().getUser().getFullName())
=======
                .clientAvatarUrl(jobPost.getClientProfile().getUser().getAvatarUrl())
                .companyName(jobPost.getClientProfile().getCompanyName())
                .expertName(application.getExpertProfile().getUser().getFullName())
                .expertAvatarUrl(application.getExpertProfile().getUser().getAvatarUrl())
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
                .budget(project.getAgreedPrice())
                .timeline(project.getTimeline())
                .deadlineAt(project.getDeadlineAt())
                .paymentDeadlineAt(project.getPaymentDeadlineAt())
                .currentStepCode(getCurrentStepCode(projectStatus, milestone))
                .currentStepTitle(getCurrentStepTitle(projectStatus, milestone))
                .currentStepDescription(getCurrentStepDescription(projectStatus, milestone))
                .milestoneStatus(milestone != null ? milestone.getStatus().name() : null)
                .canPayWithSepay(isClientProject && projectStatus == ProjectStatus.WAITING_ESCROW)
                .canOpenWorkspace(projectStatus == ProjectStatus.ACTIVE)
                .canViewPaymentStatus(isClientProject)
                .canViewDetails(true)
                .canViewSummary(projectStatus == ProjectStatus.COMPLETED)
                .canDownloadReceipt(isClientProject && projectStatus == ProjectStatus.COMPLETED)
                .steps(buildSteps(projectStatus, milestone))
                .startAt(project.getStartAt())
                .completedAt(project.getCompletedAt())
                .createAt(project.getCreateAt())
                .updateAt(project.getUpdateAt())
                .build();
    }

<<<<<<< HEAD
    public ProjectCardResponse toInvitationCardResponse(Invitation invitation, boolean isClientProject) {
=======
    default ProjectCardResponse toInvitationCardResponse(Invitation invitation, boolean isClientProject) {
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
        ExpertApplication application = invitation.getExpertApplication();
        JobPost jobPost = application.getJobpost();
        InvitationStatus invitationStatus = invitation.getInvitationStatus();

        return ProjectCardResponse.builder()
                .projectId(null)
                .invitationId(invitation.getInvitationId())
                .applicationId(application.getApplicationId())
                .jobPostId(jobPost.getJobPostId())
                .currentUserRole(isClientProject ? "CLIENT" : "EXPERT")
                .sourceType("INVITATION")
                .workflowStatus(getWorkflowStatus(invitationStatus))
                .title(invitation.getProjectTitle())
                .description(invitation.getFinalRequirement())
                .expectedOutput(invitation.getExpectedOutput())
                .acceptanceCriteria(invitation.getAcceptanceCriteria())
                .projectStatus(invitationStatus == InvitationStatus.ACCEPTED ? ProjectStatus.WAITING_ESCROW : null)
                .invitationStatus(invitationStatus)
                .escrowStatus(getEscrowStatus(invitationStatus))
                .paymentStatus(getPaymentStatus(invitationStatus))
                .clientName(jobPost.getClientProfile().getContactName())
<<<<<<< HEAD
                .companyName(jobPost.getClientProfile().getCompanyName())
                .expertName(application.getExpertProfile().getUser().getFullName())
=======
                .clientAvatarUrl(jobPost.getClientProfile().getUser().getAvatarUrl())
                .companyName(jobPost.getClientProfile().getCompanyName())
                .expertName(application.getExpertProfile().getUser().getFullName())
                .expertAvatarUrl(application.getExpertProfile().getUser().getAvatarUrl())
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
                .budget(invitation.getFinalOfferedPrice())
                .timeline(invitation.getFinalTimeline())
                .deadlineAt(null)
                .paymentDeadlineAt(invitationStatus == InvitationStatus.ACCEPTED ? invitation.getExpiresAt() : null)
                .currentStepCode(getCurrentStepCode(invitationStatus))
                .currentStepTitle(getCurrentStepTitle(invitationStatus))
                .currentStepDescription(getCurrentStepDescription(invitationStatus))
                .milestoneStatus(null)
                .canPayWithSepay(isClientProject && invitationStatus == InvitationStatus.ACCEPTED)
                .canOpenWorkspace(false)
                .canViewPaymentStatus(isClientProject && invitationStatus == InvitationStatus.ACCEPTED)
                .canViewDetails(true)
                .canViewSummary(false)
                .canDownloadReceipt(false)
                .steps(buildInvitationSteps(invitationStatus))
                .startAt(null)
                .completedAt(null)
                .createAt(invitation.getCreateAt())
                .updateAt(invitation.getUpdateAt())
                .build();
    }

    private String getWorkflowStatus(InvitationStatus status) {
        return switch (status) {
            case PENDING -> "WAITING_EXPERT_ACCEPT";
            case ACCEPTED -> "WAITING_ESCROW";
            case PAYMENT_EXPIRED -> "PAYMENT_EXPIRED";
            case REJECTED -> "REJECTED";
            case EXPIRED -> "EXPIRED";
        };
    }

    private String getEscrowStatus(InvitationStatus status) {
        return switch (status) {
            case ACCEPTED -> "UNPAID";
            case PAYMENT_EXPIRED -> "EXPIRED";
            case PENDING -> "WAITING_EXPERT";
            case REJECTED -> "REJECTED";
            case EXPIRED -> "EXPIRED";
        };
    }

    private String getPaymentStatus(InvitationStatus status) {
        return switch (status) {
            case ACCEPTED -> "UNPAID";
            case PAYMENT_EXPIRED -> "EXPIRED";
            case PENDING -> "WAITING_EXPERT_ACCEPT";
            case REJECTED -> "REJECTED";
            case EXPIRED -> "EXPIRED";
        };
    }

    private String getCurrentStepCode(InvitationStatus status) {
        return switch (status) {
            case PENDING -> "WAITING_EXPERT_ACCEPT";
            case ACCEPTED -> "WAITING_ESCROW";
            case PAYMENT_EXPIRED -> "PAYMENT_EXPIRED";
            case REJECTED -> "REJECTED";
            case EXPIRED -> "EXPIRED";
        };
    }

    private String getCurrentStepTitle(InvitationStatus status) {
        return switch (status) {
            case PENDING -> "Waiting for expert acceptance";
            case ACCEPTED -> "Waiting for escrow payment";
            case PAYMENT_EXPIRED -> "Payment expired";
            case REJECTED -> "Invitation rejected";
            case EXPIRED -> "Invitation expired";
        };
    }

    private String getCurrentStepDescription(InvitationStatus status) {
        return switch (status) {
            case PENDING -> "The invitation has been sent and is waiting for the expert to accept.";
            case ACCEPTED -> "The expert accepted. Client payment is required before the project starts.";
            case PAYMENT_EXPIRED -> "The payment window expired before escrow was funded.";
            case REJECTED -> "The expert rejected this invitation.";
            case EXPIRED -> "The expert did not respond before the invitation expired.";
        };
    }

    private List<ProjectStepResponse> buildInvitationSteps(InvitationStatus status) {
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

    private String getEscrowStatus(ProjectStatus status) {
        return switch (status) {
            case WAITING_ESCROW -> "UNPAID";
            case ACTIVE -> "HELD";
            case COMPLETED -> "RELEASED";
            case CANCELED -> "CANCELED";
        };
    }

    private String getPaymentStatus(ProjectStatus status) {
        return getEscrowStatus(status);
    }

    private String getCurrentStepCode(ProjectStatus projectStatus, ProjectMilestone milestone) {
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

    private String getCurrentStepTitle(ProjectStatus projectStatus, ProjectMilestone milestone) {
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

    private String getCurrentStepDescription(ProjectStatus projectStatus, ProjectMilestone milestone) {
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

    private List<ProjectStepResponse> buildSteps(ProjectStatus projectStatus, ProjectMilestone milestone) {
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

    private String getStepStatus(
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

    private ProjectStepResponse step(MilestoneStep step, String status) {
        return step(step.name(), step.getTitle(), status);
    }

    private ProjectStepResponse step(String code, String label, String status) {
        return ProjectStepResponse.builder()
                .code(code)
                .label(label)
                .status(status)
                .build();
    }
}
