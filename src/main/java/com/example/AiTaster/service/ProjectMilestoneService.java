package com.example.AiTaster.service;

import com.example.AiTaster.constant.*;
import com.example.AiTaster.dto.response.DeliverableResponse;
import com.example.AiTaster.dto.response.MilestoneEventResponse;
import com.example.AiTaster.dto.response.ProjectMilestoneResponse;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.DeliverableMapper;
import com.example.AiTaster.mapper.ProjectMilestoneMapper;
import com.example.AiTaster.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectMilestoneService {
    private final ProjectMilestoneRepo projectMilestoneRepo;
    private final DeliverableRepo deliverableRepo;
    private final ServiceFileRepo serviceFileRepo;
    private final ProjectRepo projectRepo;
    private final ProjectEscrowRepo projectEscrowRepo;
    private final ExpertProfileRepo expertProfileRepo;
    private final ClientProfileRepo clientProfileRepo;
    private final CurrentUserService currentUserService;
    private final LocalFileStorageService localFileStorageService;

    private final ProjectMilestoneMapper projectMilestoneMapper;
    private final DeliverableMapper deliverableMapper;
    private final SimpMessagingTemplate simpMessagingTemplate;

    // tạo milestone cho project ngay
    @Transactional
    public ProjectMilestone createMilestoneForProject(Project project) {
        if (projectMilestoneRepo.existsByProjectId(project.getProjectId())) {
            throw new GlobalException(400, "Milestone already exists for this project");
        }
        ProjectMilestone milestone = ProjectMilestone.builder()
                .projectId(project.getProjectId())
                .currentStep(MilestoneStep.DOCUMENT)
                .status(MilestoneStatus.WAITING_EXPERT_SUBMIT)
                .build();
        return projectMilestoneRepo.save(milestone);
    }

     // Lấy trạng thái milestone hiện tại của project
    // Trả currentStep, status, các mốc thời gian đã duyệt.
    @Transactional(readOnly = true)
    public ProjectMilestoneResponse getMilestone(Long projectId) {
        Project project = getProjectWithDetail(projectId);
        checkCurrentUser(project); // chỉ người trong project mới xem được
        return projectMilestoneMapper.toResponse(getMilestoneByProjectId(projectId));
    }

    //expert nop file cho mốc Document hoặc source_code
    //mỗi lần nộp tạo 1 diverble + 1 servic File
    //nộp: status -> WAITING_CLIENT_REVIEW
    @Transactional
    public ProjectMilestoneResponse submit(Long projectId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new GlobalException(400, "File is required");
        }
        Project project = getProjectWithDetail(projectId);
        ExpertProfile expertProfile = getCurrentExpertProfile();
        checkExpertOfProject(project, expertProfile);
        if (project.getProjectStatus() != ProjectStatus.ACTIVE) {
            throw new GlobalException(400, "Project is not active");
        }
        ProjectEscrow escrow = getEscrowByProjectId(projectId);
        if (escrow.getEscrowStatus() != EscrowStatus.HELD) {
            throw new GlobalException(400, "Escrow is not held");
        }
        ProjectMilestone milestone = getMilestoneByProjectId(projectId);
        if (milestone.getStatus() == MilestoneStatus.COMPLETED) {
            throw new GlobalException(400, "Milestone already completed");
        }

        MilestoneStep step = milestone.getCurrentStep();
        // Mốc 3 không cần expert nộp file
        if (step == MilestoneStep.FINAL_CONFIRMATION) {
            throw new GlobalException(400, "Final confirmation does not require submit");
        }
        // Chỉ được nộp khi đang chờ expert nộp, hoặc client yêu cầu làm lại
        if (milestone.getStatus() != MilestoneStatus.WAITING_EXPERT_SUBMIT
                && milestone.getStatus() != MilestoneStatus.REVISION_REQUESTED) {
            throw new GlobalException(400, "Not allowed to submit in current status");
        }
        // Mốc 2 chỉ mở khi mốc 1 đã được duyệt
        if (step == MilestoneStep.SOURCE_CODE && milestone.getStep1ApprovedAt() == null) {
            throw new GlobalException(400, "Step 1 not approved yet");
        }
        // version = max version hiện tại của (project, step) + 1
        int nextVersion = deliverableRepo.findMaxVersionByProjectIdAndStep(projectId, step) + 1;

        Deliverable deliverable =Deliverable.builder()
                .projectId(projectId)
                .milestoneId(milestone.getMilestoneId())
                .expertId(expertProfile.getExpertProfileId())
                .step(step)
                .version(nextVersion)
                .submittedAt(LocalDateTime.now())
                .build();
        // luu file ga
        String fileUrl = localFileStorageService.saveFile(file);
        Deliverable savedDeliverable = deliverableRepo.save(deliverable);
        serviceFileRepo.save(ServiceFile.builder()
                .productType(productTypeForStep(step))
                .productFile(fileUrl)
                .isActive(true)
                .deliverable(savedDeliverable)
                .expertService(null)
                .build());
        milestone.setStatus(MilestoneStatus.WAITING_CLIENT_REVIEW);
        projectMilestoneRepo.save(milestone);
        publishMilestoneEvent(
                project,
                milestone,
                "SUBMITTED",
                "Expert đã nộp file " + step.getTitle() + ", chờ bạn duyệt",
                getClientUserId(project)
        );
        return projectMilestoneMapper.toResponse(milestone);
    }
    // client reject yêu cầu làm lại
    @Transactional
    public ProjectMilestoneResponse requestRevision(Long projectId) {
        Project project = getProjectWithDetail(projectId);
        checkClientOwner(project, getCurrentClientProfile());
        ProjectMilestone projectMilestone = getMilestoneByProjectId(projectId);
        if (projectMilestone.getStatus() != MilestoneStatus.WAITING_CLIENT_REVIEW) {
            throw new GlobalException(400, "Milestone is not waiting client review");
        }
        // Mốc 3 không có gì để làm lại
        if (projectMilestone.getCurrentStep() == MilestoneStep.FINAL_CONFIRMATION) {
            throw new GlobalException(400, "Final confirmation cannot be revised");
        }projectMilestone.setStatus(MilestoneStatus.REVISION_REQUESTED);
         ProjectMilestone saveProjectMilestone = projectMilestoneRepo.save(projectMilestone);
        markLatestDeliverableReviewed(projectId, saveProjectMilestone.getCurrentStep()); // đánh dấu đã xem và yêu cầu làm lại
        // báo EXPERT phải làm lại
        publishMilestoneEvent(
                project,
                saveProjectMilestone,
                "REVISION_REQUESTED",
                "Client yêu cầu chỉnh sửa lại " + saveProjectMilestone.getCurrentStep().getTitle(),
                getExpertUserId(project)
        );
        return projectMilestoneMapper.toResponse(saveProjectMilestone);
    }


 // client chấp nhận sữ lý cho 3 mốc
 @Transactional
    public ProjectMilestoneResponse approve(Long projectId) {
        Project project = getProjectWithDetail(projectId);
     checkClientOwner(project, getCurrentClientProfile());
        ProjectMilestone projectMilestone = getMilestoneByProjectId(projectId);

        if(!projectMilestone.getStatus().equals(MilestoneStatus.WAITING_CLIENT_REVIEW)) {
            throw new GlobalException(400, "Milestone is not waiting client review");
        }
     MilestoneStep approvedStep = projectMilestone.getCurrentStep();
        LocalDateTime now = LocalDateTime.now();
        switch (projectMilestone.getCurrentStep())
        {
            case DOCUMENT -> {
                projectMilestone.setStatus(MilestoneStatus.WAITING_EXPERT_SUBMIT);
                projectMilestone.setStep1ApprovedAt(now);
                projectMilestone.setCurrentStep(MilestoneStep.SOURCE_CODE);
                markLatestDeliverableReviewed(projectId,approvedStep);

            }
            case SOURCE_CODE -> {
                projectMilestone.setStatus(MilestoneStatus.WAITING_CLIENT_REVIEW);
                projectMilestone.setStep2ApprovedAt(now);
                projectMilestone.setCurrentStep(MilestoneStep.FINAL_CONFIRMATION);
                markLatestDeliverableReviewed(projectId,approvedStep);
            }
            case FINAL_CONFIRMATION -> {
                finalConfirm(project,projectMilestone);
            }
        }
        projectMilestoneRepo.save(projectMilestone);
        //xong cả 3 mốc thì báo hoàn tất, ngược lại báo expert làm mốc tiếp
     if (projectMilestone.getStatus() == MilestoneStatus.COMPLETED) {
         publishMilestoneEvent(project, projectMilestone, "COMPLETED",
                 "Dự án đã hoàn tất, tiền đã được giải ngân cho expert",
                 getExpertUserId(project));
     } else {
         publishMilestoneEvent(project, projectMilestone, "APPROVED",
                 "Client đã duyệt " + approvedStep.getTitle() + ", mời expert làm bước tiếp theo",
                 getExpertUserId(project));
     }
        return projectMilestoneMapper.toResponse(projectMilestone);
    }

    //hoàn tất bước 3 -> released tiền cho expert
    private void finalConfirm(Project project,ProjectMilestone milestone ) {
        if (project.getProjectStatus() != ProjectStatus.ACTIVE) {
            throw new GlobalException(400, "Project is not active");
        }
        if (milestone.getStep1ApprovedAt() == null || milestone.getStep2ApprovedAt() == null) {
            throw new GlobalException(400, "Previous steps not approved");
        }
        if (milestone.getStatus() == MilestoneStatus.COMPLETED) {
            throw new GlobalException(400, "Milestone already completed");
        }
        if (milestone.getCurrentStep() != MilestoneStep.FINAL_CONFIRMATION) {
            throw new GlobalException(400, "Milestone is not at final confirmation step");
        }


        ProjectEscrow projectEscrow = getEscrowByProjectId(project.getProjectId());
        //chặn trả tiền 2 lần
        if(!projectEscrow.getEscrowStatus().equals(EscrowStatus.HELD)) {
           throw new GlobalException(400, "Escrow is not HELD");
        }
        milestone.setFinalApprovedAt(LocalDateTime.now());
        milestone.setStatus(MilestoneStatus.COMPLETED);
        projectMilestoneRepo.save(milestone);

        project.setProjectStatus(ProjectStatus.COMPLETED);
        project.setCompletedAt(LocalDateTime.now());
        projectRepo.save(project);

        projectEscrow.setEscrowStatus(EscrowStatus.RELEASED);
        projectEscrowRepo.save(projectEscrow);

    }

    // lấy bản Deliverable file mới nhất của mốc đang xữ lý + danh sách file để client xem
    //Client dùng để tải file qua productFile.
    public DeliverableResponse getDetailDeliverable(Long projectId) {
        Project project = getProjectWithDetail(projectId);
        checkCurrentUser(project);
        ProjectMilestone projectMilestone = getMilestoneByProjectId(projectId);
        MilestoneStep deliverableStep = projectMilestone.getCurrentStep();

        if (deliverableStep == MilestoneStep.FINAL_CONFIRMATION) {
            deliverableStep = MilestoneStep.SOURCE_CODE;
        }
        Deliverable deliverable = deliverableRepo.findTopByProjectIdAndStepOrderByVersionDesc(projectId,projectMilestone.getCurrentStep()).orElseThrow(()->  new  GlobalException(404, "No deliverable yet"));
        return deliverableMapper.toResponse(deliverable);
    }

    // show toàn bộ sản phẩm bàn giao mọi mốc , mọi version
    public List<DeliverableResponse> findDeliverables(Long projectId) {
        Project project = getProjectWithDetail(projectId);
        checkCurrentUser(project);
        return deliverableRepo.findByProjectIdOrderBySubmittedAtDesc(projectId).stream().map(deliverableMapper ::toResponse).toList();
    }


    // client xem sản phẩm bàn giao mới nhất thì set time vào
    private void markLatestDeliverableReviewed(Long projectId, MilestoneStep step) {
        deliverableRepo.findTopByProjectIdAndStepOrderByVersionDesc(projectId, step)
                .ifPresent(d -> {
                    d.setReviewedAt(LocalDateTime.now());
                    deliverableRepo.save(d);
                });
    }
    // từ mốc nộp suy ra được loại file đc phép nộp cho từng mốc
     private ProductType productTypeForStep(MilestoneStep step) {
         return switch (step) {
             case DOCUMENT ->  ProductType.DOCUMENT;
             case SOURCE_CODE ->   ProductType.SOURCE_CODE;
             case FINAL_CONFIRMATION -> ProductType.OTHER;
         };
     }

    // lấy project + querry profile , invi
    private Project getProjectWithDetail(Long projectId) {
        return projectRepo.findWithDetailByProjectId(projectId)
                .orElseThrow(() -> new GlobalException(404, "Project not found"));
    }

    // lấy milestone từ projectId
    private ProjectMilestone getMilestoneByProjectId(Long projectId){
        return projectMilestoneRepo.findByProjectId(projectId).orElseThrow(() -> new GlobalException(404, "Milestone not found"));
    }
    //lấy escrow theo projectId
    private ProjectEscrow getEscrowByProjectId(Long projectId){
        return projectEscrowRepo.findByProjectId(projectId).orElseThrow(() -> new GlobalException(404, "Project escrow not found"));
    }

    // user là expet thuộc project
    private void checkExpertOfProject(Project project, ExpertProfile expertProfile){
        if(!getExpertIdForProject(project).equals(expertProfile.getExpertProfileId()))
        {
            throw new GlobalException(403, "You are not the expert of this project");
        }
    }

    private ExpertProfile getCurrentExpertProfile() {
        User user = currentUserService.getCurrentUser();
        return expertProfileRepo.findByUser(user)
                .orElseThrow(() -> new GlobalException(403, "Only expert can use this API"));
    }

    private ClientProfile getCurrentClientProfile() {
        User user = currentUserService.getCurrentUser();
        return clientProfileRepo.findByUser(user)
                .orElseThrow(() -> new GlobalException(403, "Only client can use this API"));
    }

    //  user hiện tại là client chủ project
    private void checkClientOwner(Project project, ClientProfile clientProfile){
        if(!getClientIdForProject(project).equals(clientProfile.getClientProfileId())) {
            throw new GlobalException(403, "You are not the client owner of this project");
        }
    }

    private Long getClientIdForProject(Project project){
        return project.getInvitation().getExpertApplication().getJobpost().getClientProfile().getClientProfileId();
    }

    private Long getExpertIdForProject(Project project){
        return project.getInvitation().getExpertApplication().getExpertProfile().getExpertProfileId();
    }

    private void checkCurrentUser(Project project) {
        User user = currentUserService.getCurrentUser();
        Boolean isExpert = expertProfileRepo.findByUser(user).map(e -> e.getExpertProfileId().equals(getExpertIdForProject(project))).orElse(false);
        Boolean isClient = clientProfileRepo.findByUser(user).map(e -> e.getClientProfileId().equals(getClientIdForProject(project))).orElse(false);
        if (!isExpert && !isClient) {
            throw new GlobalException(403, "You are not a participant of this project");
        }
    }
    // tạo form thông báo milestone tới cả 2 người trong project + 1 topic riêng cho người nhận
    private void publishMilestoneEvent(
            Project project,
            ProjectMilestone milestone,
            String eventType,
            String message,
            Long targetUserId
    ) {
        MilestoneEventResponse event = MilestoneEventResponse.builder()
                .projectId(project.getProjectId())
                .eventType(eventType)
                .currentStep(milestone.getCurrentStep())
                .status(milestone.getStatus())
                .message(message)
                .targetUserId(targetUserId)
                .at(java.time.LocalDateTime.now())
                .build();
        // Cả client + expert đang xem project đều nhận
        simpMessagingTemplate.convertAndSend(
                "/topic/projects/" + project.getProjectId() + "/milestone",
                event
        );
        // Thông báo riêng cho người cần hành động tiếp theo
        if (targetUserId != null) {
            simpMessagingTemplate.convertAndSend(
                    "/topic/users/" + targetUserId + "/notifications",
                    event
            );
        }
    }
    // Lấy userId của expert trong project
    private Long getExpertUserId(Project project) {
        return project.getInvitation()
                .getExpertApplication()
                .getExpertProfile()
                .getUser()
                .getUserId();
    }
    // Lấy userId của client trong project
    private Long getClientUserId(Project project) {
        return project.getInvitation()
                .getExpertApplication()
                .getJobpost()
                .getClientProfile()
                .getUser()
                .getUserId();
    }
}
