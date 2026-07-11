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
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    default ProjectCardResponse toCardResponse(
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
                .clientAvatarUrl(jobPost.getClientProfile().getUser().getAvatarUrl())
                .companyName(jobPost.getClientProfile().getCompanyName())
                .expertName(application.getExpertProfile().getUser().getFullName())
                .expertAvatarUrl(application.getExpertProfile().getUser().getAvatarUrl())
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

    default ProjectCardResponse toInvitationCardResponse(Invitation invitation, boolean isClientProject) {
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
                .clientAvatarUrl(jobPost.getClientProfile().getUser().getAvatarUrl())
                .companyName(jobPost.getClientProfile().getCompanyName())
                .expertName(application.getExpertProfile().getUser().getFullName())
                .expertAvatarUrl(application.getExpertProfile().getUser().getAvatarUrl())
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
            case PENDING -> "Chờ chuyên gia chấp nhận";
            case ACCEPTED -> "Chờ thanh toán ký quỹ";
            case PAYMENT_EXPIRED -> "Thanh toán đã hết hạn";
            case REJECTED -> "Lời mời đã bị từ chối";
            case EXPIRED -> "Lời mời đã hết hạn";
        };
    }

    private String getCurrentStepDescription(InvitationStatus status) {
        return switch (status) {
            case PENDING -> "Lời mời đã được gửi và đang chờ chuyên gia chấp nhận.";
            case ACCEPTED -> "Chuyên gia đã chấp nhận. Khách hàng cần thanh toán trước khi dự án bắt đầu.";
            case PAYMENT_EXPIRED -> "Thời gian thanh toán đã hết trước khi ký quỹ được nạp.";
            case REJECTED -> "Chuyên gia đã từ chối lời mời này.";
            case EXPIRED -> "Chuyên gia không phản hồi trước khi lời mời hết hạn.";
        };
    }

    private List<ProjectStepResponse> buildInvitationSteps(InvitationStatus status) {
        if (status == InvitationStatus.ACCEPTED) {
            return List.of(
                    step("EXPERT_ACCEPT", "Chuyên gia chấp nhận", "DONE"),
                    step("ESCROW_PAYMENT", "Thanh toán ký quỹ", "CURRENT"),
                    step("PROJECT_START", "Bắt đầu dự án", "LOCKED")
            );
        }

        if (status == InvitationStatus.PENDING) {
            return List.of(
                    step("EXPERT_ACCEPT", "Chuyên gia chấp nhận", "CURRENT"),
                    step("ESCROW_PAYMENT", "Thanh toán ký quỹ", "LOCKED"),
                    step("PROJECT_START", "Bắt đầu dự án", "LOCKED")
            );
        }

        return List.of(
                step("EXPERT_ACCEPT", "Chuyên gia chấp nhận", "LOCKED"),
                step("ESCROW_PAYMENT", "Thanh toán ký quỹ", "LOCKED"),
                step("PROJECT_START", "Bắt đầu dự án", "LOCKED")
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
            return "Chờ thanh toán ký quỹ";
        }
        if (projectStatus == ProjectStatus.COMPLETED) {
            return "Tất cả mốc đã hoàn tất";
        }
        if (projectStatus == ProjectStatus.CANCELED) {
            return "Dự án đã hủy";
        }
        if (milestone == null || milestone.getCurrentStep() == null) {
            return "Tài liệu";
        }
        return milestone.getCurrentStep().getTitle();
    }

    private String getCurrentStepDescription(ProjectStatus projectStatus, ProjectMilestone milestone) {
        if (projectStatus == ProjectStatus.WAITING_ESCROW) {
            return "Khách hàng cần thanh toán trước khi dự án bắt đầu.";
        }
        if (projectStatus == ProjectStatus.COMPLETED) {
            return "Tất cả mốc dự án đã được phê duyệt.";
        }
        if (projectStatus == ProjectStatus.CANCELED) {
            return "Dự án này không còn hoạt động.";
        }
        if (milestone == null || milestone.getStatus() == null) {
            return "Không gian làm việc của dự án đã sẵn sàng.";
        }

        return switch (milestone.getStatus()) {
            case WAITING_EXPERT_SUBMIT -> "Đang chờ chuyên gia nộp sản phẩm bàn giao.";
            case WAITING_CLIENT_REVIEW -> "Đang chờ khách hàng duyệt.";
            case REVISION_REQUESTED -> "Khách hàng đã yêu cầu chỉnh sửa.";
            case COMPLETED -> "Tất cả mốc đã hoàn tất.";
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
